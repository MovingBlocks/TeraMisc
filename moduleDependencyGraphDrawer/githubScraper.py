import requests
import json

modules = []
dependencies = []  # list of tuples: (dependantPackage, dependency)


def getUrls():
    nextPage = "https://api.github.com/orgs/Terasology/repos?per_page=100"
    repos = []
    while nextPage is not None:
        r = requests.get(nextPage)
        header_link: str = r.headers.get("Link")
        if header_link is None:
            nextPage = None
        else:
            nextPageLink = [x.split(";")[0] for x in header_link.split(",") if "next" in x.split(";")[1]]
            if len(nextPageLink) == 0:
                nextPage = None
            else:
                nextPage = nextPageLink[0][1:-1]
        repos += json.loads(r.content)
    return [(x["html_url"]+"/"+x["default_branch"]+"/module.txt").replace("github", "raw.githubusercontent", 1) for x in repos]


def init():
    print("Fetching list of modules... ", end="")
    urls = getUrls()
    print("Done")
    modules.clear()
    dependencies.clear()
    print("Fetching module files:")
    modulesfetched = 0
    for url in urls:
        r = requests.get(url)
        modulesfetched += 1
        if r.status_code != 200:
            print(f"  Skipping non-module - [{modulesfetched}/{len(urls)}]")
            continue
        module = json.loads(r.content)
        modules.append(module["id"])
        for dependency in module["dependencies"]:
            dependencies.append((module["id"], dependency["id"]))
        print(f"  Done {module['id']} - [{modulesfetched}/{len(urls)}]")
