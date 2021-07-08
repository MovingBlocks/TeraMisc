import networkx as nx
import matplotlib.pyplot as plt
import githubScraper

githubScraper.init()
G = nx.DiGraph()
G.add_nodes_from(githubScraper.modules)
G.add_edges_from(githubScraper.dependencies)
plt.figure(1, figsize=(100, 60))
nx.draw_kamada_kawai(G, with_labels=True, node_size=60, font_size=8, font_color="b", font_weight="bold")
plt.savefig("deps.png", dpi=300)
