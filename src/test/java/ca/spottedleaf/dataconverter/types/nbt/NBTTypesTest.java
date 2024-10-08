package ca.spottedleaf.dataconverter.types.nbt;

import ca.spottedleaf.dataconverter.types.ObjectType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NBTTypesTest {

    @Test
    public void test() {
        NBTMapType map1 = new NBTMapType();

        NBTMapType map2 = new NBTMapType();
        map1.setMap("test", map2);

        map2.setString("test", "test");
        map2.setByte("test2", (byte) 1);
        map2.setShort("test3", (short) 1);

        NBTListType list = new NBTListType();
        map2.setList("list", list);
        list.addString("test");
        list.addString("test2");

        Assertions.assertEquals("test", map1.getMap("test").getString("test"));
        Assertions.assertEquals((byte) 1, map1.getMap("test").getByte("test2"));
        Assertions.assertEquals((short) 1, map1.getMap("test").getShort("test3"));
        Assertions.assertEquals("test", map1.getMap("test").getList("list", ObjectType.STRING).getString(0));
    }


}
