import os

import bpy
import bpy_extras.io_utils
import json
import datetime
from mathutils import Vector
from . import constants
import bmesh

class ImportToBlockShape(bpy.types.Operator, bpy_extras.io_utils.ImportHelper):
    bl_idname = "import_scene.shape"
    bl_label = "Import Terasology Block Shape"

    filename_ext = ".shape"

    def execute(self, context):
        path = bpy.path.ensure_ext(self.filepath, self.filename_ext)

        if bpy.ops.object.mode_set.poll():
            bpy.ops.object.mode_set(mode='OBJECT')

        if bpy.ops.object.select_all.poll():
            bpy.ops.object.select_all(action='DESELECT')

        file = open(path)
        payload = json.loads(file.read())

        if 'displayName' in payload:
            context.scene.teraDisplayName = payload['displayName']
        if 'author' in payload:
            context.scene.teraAuthor = payload['author']


        for part in constants.PARTS:
            if(part.lower() in payload):
                sub_payload = payload[part.lower()]

                bm = bmesh.new()
                verticies = []
                for v in sub_payload['vertices']:
                    verticies.append(bm.verts.new((-v[0],v[1],v[2])))

                bm.verts.ensure_lookup_table()
                bm.verts.index_update()

                for face_index in sub_payload['faces']:
                    bm.faces.new([verticies[i] for i in face_index])

                uv_layer = bm.loops.layers.uv.new()
                for face in bm.faces:
                    for loop in face.loops:
                        uv = sub_payload['texcoords'][loop.vert.index]
                        loop[uv_layer].uv = (uv[0],1.0 - uv[1])

                mesh = bpy.data.meshes.new("mesh")
                bm.to_mesh(mesh)
                object = bpy.data.objects.new(part.capitalize(),mesh)
                bpy.context.scene.objects.link(object)
                bm.free()

                if 'fullSide' in  sub_payload:
                    bpy.data.objects[part.capitalize()].teraFullSide = sub_payload['fullSide']
                else:
                    bpy.data.objects[part.capitalize()].teraFullSide = False


        payload_collision = None
        if 'collision' in payload:
            payload_collision = payload['collision']
        if(payload_collision != None):
            if 'symmetric' in payload_collision:
                context.scene.teraCollisionSymmetric = payload_collision['symmetric']
            if 'yawSymmetric' in payload_collision:
                context.scene.teraCollisionSymmetricZ = payload_collision['yawSymmetric']
            if 'pitchSymmetric' in payload_collision:
                context.scene.teraCollisionSymmetricX = payload_collision['pitchSymmetric']
            if 'rollSymmetric' in payload_collision:
                context.scene.teraCollisionSymmetricY = payload_collision['rollSymmetric']
            if 'convexHull' in payload_collision:
                context.scene.teraCollisionSymmetric = payload_collision['convexHull']
                context.scene.teraCollisionType = "ConvexHull"
            else:
                if 'colliders' in payload_collision:
                    for collider in payload_collision['colliders']:
                        if(collider['type'] == 'AABB'):
                            p = collider['position']
                            e = collider['extents']
                            bpy.ops.mesh.primitive_cube_add(location=(-p[0], p[1], p[2]))
                            bpy.ops.transform.resize(value=(-e[0], e[1], e[2]))
                            bpy.ops.object.transform_apply(location=True, scale=True, rotation=True)
                            obj = bpy.context.object
                            obj['teraColliderType'] = collider['type']
                        elif collider['type'] == 'Sphere':
                            p = collider['position']
                            r = collider['radius']
                            bpy.ops.mesh.primitive_uv_sphere_add(segments=16,location = (-p[0], p[1], p[2]),size = r)
                            bpy.ops.object.transform_apply(location=True, scale=True, rotation=True)
                            obj = bpy.context.object
                            obj['teraColliderType'] = collider['type']

        return {'FINISHED'}
