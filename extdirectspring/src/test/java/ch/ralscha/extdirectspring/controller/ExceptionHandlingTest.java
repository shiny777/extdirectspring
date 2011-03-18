/**
 * Copyright 2010 Ralph Schaer <ralphschaer@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ralscha.extdirectspring.controller;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ch.ralscha.extdirectspring.bean.ExtDirectRequest;
import ch.ralscha.extdirectspring.bean.ExtDirectResponse;
import ch.ralscha.extdirectspring.bean.ExtDirectStoreReadRequest;
import ch.ralscha.extdirectspring.bean.ExtDirectStoreResponse;
import ch.ralscha.extdirectspring.provider.Row;
import ch.ralscha.extdirectspring.util.ExtDirectSpringUtil;

/**
 * Tests for {@link RouterController}.
 * 
 * @author Ralph Schaer
 */
@SuppressWarnings("all")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/testExceptionHandling.xml")
public class ExceptionHandlingTest {

	@Inject
	private RouterController controller;

	private MockHttpServletResponse response;
	private MockHttpServletRequest request;

	@Before
	public void beforeTest() {
		response = new MockHttpServletResponse();
		request = new MockHttpServletRequest();
	}
	
	@Test
	public void testDefault() throws Exception {
		ExtDirectResponse resp = runTest(null);
		assertEquals("Server Error", resp.getMessage());
		assertNull(resp.getWhere());
	}

	@Test
	public void testDefaultExceptionMessage() throws Exception {
		Configuration configuration = new Configuration();
		configuration.setDefaultExceptionMessage("an error occured");
		ExtDirectResponse resp = runTest(configuration);
		assertEquals("an error occured", resp.getMessage());
		assertNull(resp.getWhere());
	}

	@Test
	public void testExceptionNameAsMessage() throws Exception {
		Configuration configuration = new Configuration();
		configuration.setSendExceptionMessage(true);
		ExtDirectResponse resp = runTest(configuration);
		assertEquals("Invalid remoting method 'remoteProviderSimple.method4'. Missing ExtDirectMethod annotation", resp.getMessage());
		assertNull(resp.getWhere());
	}

	@Test
	public void testExceptionToMessage() throws Exception {
		Configuration configuration = new Configuration();
		Map<Class<?>, String> exceptionMessageMapping = new HashMap<Class<?>, String>();
		exceptionMessageMapping.put(IllegalArgumentException.class, "illegal argument");
		configuration.setExceptionToMessage(exceptionMessageMapping);
		ExtDirectResponse resp = runTest(configuration);
		assertEquals("illegal argument", resp.getMessage());
		assertNull(resp.getWhere());
	}

	@Test
	public void testDefaultExceptionMessageWithStacktrace() throws Exception {
		Configuration configuration = new Configuration();
		configuration.setSendStacktrace(true);
		configuration.setDefaultExceptionMessage("an error occured");
		ExtDirectResponse resp = runTest(configuration);
		assertEquals("an error occured", resp.getMessage());
		assertNotNull(resp.getWhere());
		assertTrue(resp
				.getWhere()
				.startsWith(
						"java.lang.IllegalArgumentException: Invalid remoting method 'remoteProviderSimple.method4'. Missing ExtDirectMethod annotation"));
	}

	@Test
	public void testExceptionNameAsMessageWithStacktrace() throws Exception {
		Configuration configuration = new Configuration();
		configuration.setSendStacktrace(true);
		configuration.setSendExceptionMessage(true);
		ExtDirectResponse resp = runTest(configuration);
		assertEquals("Invalid remoting method 'remoteProviderSimple.method4'. Missing ExtDirectMethod annotation", resp.getMessage());
		assertNotNull(resp.getWhere());
		assertTrue(resp
				.getWhere()
				.startsWith(
						"java.lang.IllegalArgumentException: Invalid remoting method 'remoteProviderSimple.method4'. Missing ExtDirectMethod annotation"));

	}

	@Test
	public void testExceptionToMessageWithStacktrace() throws Exception {
		Configuration configuration = new Configuration();
		configuration.setSendStacktrace(true);
		Map<Class<?>, String> exceptionMessageMapping = new HashMap<Class<?>, String>();
		exceptionMessageMapping.put(IllegalArgumentException.class, "illegal argument");
		configuration.setExceptionToMessage(exceptionMessageMapping);
		ExtDirectResponse resp = runTest(configuration);
		assertEquals("illegal argument", resp.getMessage());
		assertNotNull(resp.getWhere());
		assertTrue(resp
				.getWhere()
				.startsWith(
						"java.lang.IllegalArgumentException: Invalid remoting method 'remoteProviderSimple.method4'. Missing ExtDirectMethod annotation"));
	}

	private ExtDirectResponse runTest(Configuration configuration) throws Exception {
		controller.setConfiguration(configuration);
		controller.afterPropertiesSet();

		String json = ControllerUtil.createRequestJson("remoteProviderSimple", "method4", 2, 3, 2.5, "string.param");
		List<ExtDirectResponse> responses = controller.router(request, response, Locale.ENGLISH, json);

		assertEquals(1, responses.size());
		ExtDirectResponse resp = responses.get(0);
		assertEquals("remoteProviderSimple", resp.getAction());
		assertEquals("method4", resp.getMethod());
		assertEquals("exception", resp.getType());
		assertEquals(2, resp.getTid());
		assertNull(resp.getResult());

		return resp;
	}

}
