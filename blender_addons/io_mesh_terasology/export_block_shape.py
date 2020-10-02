import bpy
from bpy.props import FloatProperty, IntProperty, BoolProperty, StringProperty, CollectionProperty, FloatVectorProperty, \
    EnumProperty, IntVectorProperty
import bpy_extras.io_utils
import json
import datetime
from mathutils import Vector
from . import constants

class ExportToBlockShape(bpy.types.Operator, bpy_extras.io_utils.ExportHelper):
    bl_idname = "export_scene.shape"
    bl_label = "Export Terasology Block Shape"

    filename_ext = ".shape"
    filter_glob = StringProperty(default="*.shape", options={'HIDDEN'})

    apply_modifiers = BoolProperty(
        name="Apply Modifiers",
        description="Apply Modifiers to the exported mesh",
        default=True)

    @classmethod
    def poll(cls, context):
        return context.active_object != None

    def meshify(self, obj, scene, apply_modifiers):
        if not obj:
            return

        mesh = None
        if apply_modifiers:
            mesh = obj.to_mesh(scene, True, 'PREVIEW')
        else:
            mesh = obj.data
        mesh.update(calc_tessface=True)

        result = {}
        result['vertices'] = []
        result['normals'] = []
        result['texcoords'] = []
        result['faces'] = []

        temp_verts = []
        for v in mesh.vertices:
            result['vertices'].append([-v.co[0], v.co[2], v.co[1]])
            result['normals'].append(None)
            result['texcoords'].append(None)

        for i, face in enumerate(mesh.tessfaces):
            for j, index in enumerate(face.vertices):
                vert = mesh.vertices[index]
                if scene.teraBillboardNormals:
                    normal = [0, 0, 1]
                elif face.use_smooth:
                    normal = tuple(face.normal)
                else:
                    normal = tuple(vert.normal)
                uvtemp = mesh.tessface_uv_textures.active.data[i].uv[j]
                uvs = uvtemp[0], 1.0 - uvtemp[1]
                result['normals'][index] = [-normal[0], normal[2], normal[1]]
                result['texcoords'][index] = uvs
            result['faces'].append([f for f in face.vertices])

        if apply_modifiers:
            bpy.data.meshes.remove(mesh)

        return result

    def AABBCollider(self, objs):

        min = [100000.0, 100000.0, 100000.0]
        max = [-100000.0, -100000.0, -100000.0]

        for obj in objs:
            if not obj:
                return
            mesh = obj.data
            for face in mesh.faces:
                for index in enumerate(face.vertices):
                    vert = mesh.vertices[index].co
                    for i in range(3):
                        if vert[i] > max[i]:
                            max[i] = vert[i]
                        elif vert[i] < min[i]:
                            min[i] = vert[i]

        pos = [0.0, 0.0, 0.0]
        dim = [0.0, 0.0, 0.0]

        for i in range(3):
            pos[i] = 0.5 * (max[i] + min[i])
            dim[i] = 0.5 * (max[i] - min[i])

        return {
            'type': 'AABB',
            'position': [-pos[0], pos[2], pos[1]],
            'extents': [-dim[0], dim[2], dim[1]]
        }

    def sphereCollider(self, objs):

        center = Vector((0, 0, 0))
        radius = 0.0
        for obj in objs:
            if not obj:
                return
            mesh = obj.data
            for v in mesh.vertices:
                center += v.co
            center /= len(mesh.vertices)
            for v in mesh.vertices:
                dist = (center - v.co).length
                radius = max(dist, radius)
        return {
            'type': 'Sphere',
            'position': [-center[0], center[2], center[1]],
            'radius': radius
        }

    def execute(self, context):
        path = bpy.path.ensure_ext(self.filepath, self.filename_ext)

        result = {}
        result['displayName'] = context.scene.teraDisplayName
        result['author'] = context.scene.teraAuthor

        now = datetime.datetime.now()
        result['exportDate'] = '{:%Y-%m-%d %H:%M:%S}'.format(now)

        bpy.ops.object.mode_set(mode='OBJECT')
        for part in constants.PARTS:
            if part in bpy.data.objects:
                result[part.lower()] = self.meshify(bpy.data.objects[part], context.scene, self.apply_modifiers)
                if ("teraFullSide" in bpy.data.objects[part]):
                    result[part.lower()]['fullSide'] = bpy.data.objects[part].teraFullSide
                else:
                    result[part.lower()]['fullSide'] = False

        hasColliders = False
        result['collision'] = {}
        if context.scene.teraCollisionType == "AutoAABB":
            hasColliders = True
            result['collision']['colliders'] = [self.AABBCollider([o for o in bpy.data.objects if o.name in constants.PARTS])]
        elif context.scene.teraCollisionType == "ConvexHull":
            result['collision']['convexHull'] = True
            hasColliders = True
        elif context.scene.teraCollisionType == "Manual":
            hasColliders = True
            result['collision']['colliders'] = []
            for object in bpy.data.objects:
                if object.teraColliderType != '' and object.teraColliderType == 'None':
                    if object.teraColliderType == 'AABB':
                        result['collision']['colliders'].append(self.AABBCollider([object]))
                    elif object.teraColliderType == 'Sphere':
                        result['collision']['colliders'].append(self.sphereCollider([object]))

        if (hasColliders == True):
            result['collision']["symmetric"] = context.scene.teraCollisionSymmetric
            result['collision']['yawSymmetric'] = context.scene.teraCollisionSymmetricZ
            result['collision']['pitchSymmetric'] = context.scene.teraCollisionSymmetricX
            result['collision']['rollSymmetric'] = context.scene.teraCollisionSymmetricY

        file = open(path, "w", encoding="utf8")
        print("saving complete: %r " % path)
        file.write(json.dumps(result,  indent=4, separators=(',', ': ')))
        file.close()
        # filepath = self.filepath

        # from . import _export_block_shape
        # keywords = self.as_keywords(ignore=("filter_glob", "check_existing"))
        return {'FINISHED'}

    def draw(self, context):
        layout = self.layout

        row = layout.row()
        row.prop(self, "apply_modifiers")
