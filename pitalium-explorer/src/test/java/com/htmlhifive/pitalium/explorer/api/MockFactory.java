/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.pitalium.explorer.api;

import org.mockito.Mockito;
import org.springframework.beans.factory.FactoryBean;

public class MockFactory<T> implements FactoryBean<T> {

	private Class<T> mockClass;

	public MockFactory(Class<T> mockClass) {
		this.mockClass = mockClass;
	}

	@Override
	public T getObject() throws Exception {
		return Mockito.mock(this.mockClass);
	}

	@Override
	public Class<?> getObjectType() {
		return this.mockClass;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
