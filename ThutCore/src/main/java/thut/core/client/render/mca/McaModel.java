package thut.core.client.render.mca;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import thut.core.client.render.mca.McaXML.Buffers;
import thut.core.client.render.mca.McaXML.Children;
import thut.core.client.render.mca.McaXML.GeometryNode;
import thut.core.client.render.mca.McaXML.Rot;
import thut.core.client.render.mca.McaXML.SceneNode;
import thut.core.client.render.mca.McaXML.Translation;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.x3d.Shape;
import thut.core.client.render.x3d.X3dModel;
import thut.core.client.render.x3d.X3dObject;

public class McaModel extends X3dModel
{
    // public McaModel(ResourceLocation l)
    // {
    // super();
    // loadModel(l);
    // }

    public McaModel(InputStream l)
    {
        super();
        loadModel(l);
    }

    // public void loadModel(ResourceLocation model)
    // {
    // try
    // {
    // IResource res =
    // Minecraft.getMinecraft().getResourceManager().getResource(model);
    // InputStream stream = res.getInputStream();
    // McaXML xml = new McaXML(stream);
    // makeObjects(xml);
    // }
    // catch (Exception e)
    // {
    // e.printStackTrace();
    // }
    // }

    public void loadModel(InputStream stream)
    {
        try
        {
            McaXML xml = new McaXML(stream);
            makeObjects(xml);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void addChildren(Set<Children> set, Children node)
    {
        if (node.geometry != null)
        {
            set.add(node);
        }
        for (SceneNode child : node.scenes)
        {
            child.children.parent = child;
            addChildren(set, child.children);
        }
    }

    private Set<String> getChildren(Children parent)
    {
        Set<String> ret = Sets.newHashSet();
        for (SceneNode child : parent.scenes)
        {
            if (child.children.geometry != null)
            {
                ret.add(child.name);
            }
        }
        return ret;
    }

    HashMap<String, IExtendedModelPart> makeObjects(McaXML xml) throws Exception
    {
        Set<Children> scenes = Sets.newHashSet();
        addChildren(scenes, xml.model.node.children);
        Map<String, Set<String>> childMap = Maps.newHashMap();

        for (Children node : scenes)
        {
            GeometryNode geom = node.geometry;
            Translation trans = node.parent.transform.translation;
            Rot rot = node.parent.transform.rotation;
            Buffers buffers = geom.mesh.buffers;
            String name = node.parent.name;
            X3dObject o = new X3dObject(name);
            List<Shape> shapes = Lists.newArrayList();
            Shape shape = new Shape(buffers.getOrder(), buffers.getVerts(), buffers.getNormals(), buffers.getTex());
            shapes.add(shape);
            o.shapes = shapes;

            // o.offset.set(bound.x / 16f, bound.y / 16f, bound.z / 16f);
            if (trans != null) o.offset.set(trans.x / 16f, trans.y / 16f, trans.z / 16f);
            if (rot != null) o.rotations.set(rot.x, rot.y, rot.z, rot.w);

            Set<String> children = getChildren(node);
            if (!children.isEmpty()) childMap.put(name, children);
            parts.put(name, o);
        }
        System.out.println(childMap.size());
        System.out.println(parts.size() + " parts");
        for (Map.Entry<String, Set<String>> entry : childMap.entrySet())
        {
            String key = entry.getKey();

            if (parts.get(key) != null)
            {
                IExtendedModelPart part = parts.get(key);
                for (String s : entry.getValue())
                {
                    if (parts.get(s) != null && parts.get(s) != part) part.addChild(parts.get(s));
                }
            }
        }
        return parts;
    }
}
