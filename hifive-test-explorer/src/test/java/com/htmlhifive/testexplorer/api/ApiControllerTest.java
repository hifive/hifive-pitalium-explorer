package com.htmlhifive.testexplorer.api;

import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.htmlhifive.testexplorer.entity.TestExecution;
import com.htmlhifive.testexplorer.entity.TestExecutionRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/test-context.xml")
public class ApiControllerTest {
	@Autowired
	private ApiController apiController;
	
	@Autowired
	private TestExecutionRepository testExecutionRepo;
	
	@Test
	public void testListTestExecution()
	{
		ArrayList<TestExecution> executionList = new ArrayList<TestExecution>();
		TestExecution testExecution1 = new TestExecution();
		testExecution1.setId(17);
		testExecution1.setLabel("API TEST LABEL");
		testExecution1.setTime(new Timestamp(111111));
		executionList.add(testExecution1);
		when(testExecutionRepo.findAll()).thenReturn(executionList);
		
		ResponseEntity<List<TestExecution>> response = this.apiController.listTestExecution();
		
		Assert.assertEquals(200, response.getStatusCode().value());
		Assert.assertEquals(executionList, response.getBody());
		verify(testExecutionRepo).findAll();
	}
}
