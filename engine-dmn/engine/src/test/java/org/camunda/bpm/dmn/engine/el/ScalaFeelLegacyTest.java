/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.dmn.engine.el;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.camunda.bpm.dmn.feel.impl.FeelException;
import org.camunda.feel.integration.CamundaFeelEngineFactory;
import org.junit.Test;

public class ScalaFeelLegacyTest extends DmnEngineTest {

  protected static final String FEEL_TEST_DMN = "FeelTest.dmn";
  protected static final String EMPTY_EXPRESSIONS_DMN = "org/camunda/bpm/dmn/engine/el/ExpressionLanguageTest.emptyExpressions.dmn";
  protected static final String DMN = "org/camunda/bpm/dmn/engine/el/FeelIntegrationTest.dmn";
  protected static final String DMN_12 = "org/camunda/bpm/dmn/engine/el/dmn12/FeelIntegrationTest.dmn";

  @Override
  public DmnEngineConfiguration getDmnEngineConfiguration() {
    DefaultDmnEngineConfiguration configuration = new DefaultDmnEngineConfiguration();
    configuration.setFeelEngineFactory(new CamundaFeelEngineFactory());
    return configuration;
  }

  @Test
  @DecisionResource(resource = EMPTY_EXPRESSIONS_DMN, decisionKey = "decision2")
  public void testFailFeelUseOfEmptyInputExpression() {
    try {
      evaluateDecisionTable();
      failBecauseExceptionWasNotThrown(FeelException.class);
    }
    catch (FeelException e) {
      //assertThat(e).hasMessageStartingWith("FEEL-01017");
      assertThat(e).hasMessageContaining("failed to evaluate expression '10': no variable found for name 'cellInput'");
    }
  }

  @Test
  @DecisionResource(resource = FEEL_TEST_DMN)
  public void testStringVariable() {
    variables.putValue("stringInput", "camunda");
    variables.putValue("numberInput", 13.37);
    variables.putValue("booleanInput", true);

    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntry(true);
  }

}
