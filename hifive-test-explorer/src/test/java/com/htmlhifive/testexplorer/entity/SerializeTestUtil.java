package com.htmlhifive.testexplorer.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializeTestUtil {
	public Object serializeAndDeserialize(Object obj) throws IOException, ClassNotFoundException
	{
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	ObjectOutputStream oos = new ObjectOutputStream(baos);
    	oos.writeObject(obj);
    	oos.close();
    	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    	ObjectInputStream ois = new ObjectInputStream(bais);
    	return ois.readObject();
	}
}
