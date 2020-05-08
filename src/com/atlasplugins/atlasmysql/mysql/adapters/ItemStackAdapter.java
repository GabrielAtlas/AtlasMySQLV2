package com.atlasplugins.atlasmysql.mysql.adapters;


import com.atlasplugins.atlasmysql.mysql.helper.ReflectionUtils;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.math.BigInteger;

public class ItemStackAdapter extends TypeAdapter<ItemStack> {
	
    @Override//
    public void write(JsonWriter out, ItemStack item) throws IOException {
        out.beginObject();
        out.name("item").value(toBase64(item));
        out.endObject();
    }

    @Override
    public ItemStack read(JsonReader in) throws IOException {
        in.beginObject();

        ItemStack item = null;
        while (in.hasNext()) {
            if (in.nextName().equalsIgnoreCase("item")) {
                item = fromBase64(in.nextString());
            }
        }

        in.endObject();
        return item;
    }
    public static ItemStack fromBase64(String data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        ItemStack itemStack = null;
        try {
            Class<?> nbtTagCompoundClass = ReflectionUtils.getNMSClass("NBTTagCompound");
            Class<?> nmsItemStackClass = ReflectionUtils.getNMSClass("ItemStack");
            Object nbtTagCompound = ReflectionUtils.getNMSClass("NBTCompressedStreamTools").getMethod("a", DataInputStream.class).invoke(null, dataInputStream);
            Object craftItemStack = nmsItemStackClass.getMethod("createStack", nbtTagCompoundClass).invoke(null, nbtTagCompound);
            itemStack = (ItemStack) ReflectionUtils.getOBClass("inventory.CraftItemStack").getMethod("asBukkitCopy", nmsItemStackClass).invoke(null, craftItemStack);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return itemStack;
    }

    public static String toBase64(ItemStack item) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);

        try {
            Class<?> nbtTagCompoundClass = ReflectionUtils.getNMSClass("NBTTagCompound");
            Constructor<?> nbtTagCompoundConstructor = nbtTagCompoundClass.getConstructor();
            Object nbtTagCompound = nbtTagCompoundConstructor.newInstance();
            Object nmsItemStack = ReflectionUtils.getOBClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            ReflectionUtils.getNMSClass("ItemStack").getMethod("save", nbtTagCompoundClass).invoke(nmsItemStack, nbtTagCompound);
            ReflectionUtils.getNMSClass("NBTCompressedStreamTools").getMethod("a", nbtTagCompoundClass, DataOutput.class).invoke(null, nbtTagCompound, (DataOutput) dataOutput);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }
    
    
}