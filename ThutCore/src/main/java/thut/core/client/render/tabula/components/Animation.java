package thut.core.client.render.tabula.components;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Nullable;

import org.w3c.dom.NamedNodeMap;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.core.client.render.animation.AnimationRegistry.IPartRenamer;

/** Container for Tabula animations.
 *
 * @author Gegy1000
 * @since 0.1.0 */
@SideOnly(Side.CLIENT)
public class Animation
{
    public String name = "";
    public String identifier = "";
    public int    length = -1;

    public boolean loops = true;
    
    private Set<String> checked = Sets.newHashSet();

    public TreeMap<String, ArrayList<AnimationComponent>> sets = new TreeMap<String, ArrayList<AnimationComponent>>(
            Ordering.natural()); // cube identifier to animation component

    public ArrayList<AnimationComponent> getComponents(String key)
    {
        if(!checked.contains(key))
        {
            ArrayList<AnimationComponent> comps = null;
            for(String s: sets.keySet())
            {
                if(s.startsWith("*") && key.matches(s.substring(1)))
                {
                    comps = sets.get(s);
                    break;
                }
            }
            if(comps!=null)
            {
                sets.put(key, comps);
            }
            checked.add(key);
        }
        return sets.get(key);
    }

    public int getLength()
    {
        return length;
    }

    public Animation init(NamedNodeMap map, @Nullable IPartRenamer renamer)
    {
        return this;
    }
    
    public void initLength()
    {
        for (Entry<String, ArrayList<AnimationComponent>> entry : sets.entrySet())
        {
            for (AnimationComponent component : entry.getValue())
            {
                if (component.startKey + component.length > length)
                {
                    length = component.startKey + component.length;
                }
            }
        }
    }
    
    @Override
    public String toString()
    {
        return name + "|" + identifier + "|" + loops;
    }
}
