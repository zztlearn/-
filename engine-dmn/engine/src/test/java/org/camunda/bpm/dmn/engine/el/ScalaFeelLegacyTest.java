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
import static org.camunda.bpm.dmn.engine.util.DmnExampleVerifier.assertExample;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.camunda.bpm.dmn.feel.impl.FeelException;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.feel.integration.CamundaFeelEngineFactory;
import org.camunda.spin.Spin;
import org.junit.Ignore;
import org.junit.Test;

public class ScalaFeelLegacyTest extends DmnEngineTest {

  protected static final String FEEL_TEST_DMN = "FeelTest.dmn";
  protected static final String EMPTY_EXPRESSIONS_DMN = "org/camunda/bpm/dmn/engine/el/ExpressionLanguageTest.emptyExpressions.dmn";
  protected static final String DMN = "org/camunda/bpm/dmn/engine/el/FeelIntegrationTest.dmn";
  protected static final String DMN_12 = "org/camunda/bpm/dmn/engine/el/dmn12/FeelIntegrationTest.dmn";
  protected static final String DATE_AND_TIME_DMN = "org/camunda/bpm/dmn/engine/el/FeelIntegrationTest.testDateAndTimeIntegration.dmn";

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
      evaluateDecisionTable(dmnEngine);
      failBecauseExceptionWasNotThrown(FeelException.class);
    }
    catch (FeelException e) {
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

  @Test
  @DecisionResource(resource = DMN)
  public void testFeelInputEntry() {
    DmnDecisionResult decisionResult = dmnEngine.evaluateDecision(decision, Variables.createVariables().putValue("score", 3));
    assertThat((String)decisionResult.getSingleEntry()).isEqualTo("a");
  }

  @Test
  @DecisionResource(resource = DMN)
  public void testFeelInputEntryWithAlternativeName() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    configuration.setDefaultInputEntryExpressionLanguage("feel");
    DmnEngine dmnEngine = configuration.buildEngine();

    DmnDecisionResult decisionResult = dmnEngine.evaluateDecision(decision, Variables.createVariables().putValue("score", 3));
    assertThat((String)decisionResult.getSingleEntry()).isEqualTo("a");
  }

  @Test
  @DecisionResource(resource = "org/camunda/bpm/dmn/engine/el/ExpressionLanguageTest.script.dmn")
  public void testFeelExceptionDoesNotContainJuel() {
    try {
      assertExample(dmnEngine, decision);
      failBecauseExceptionWasNotThrown(FeelException.class);
    }
    catch (FeelException e) {
      assertThat(e).hasMessageContaining("failed to parse expression 'cellInput == \"bronze\"'");
    }
  }

  @Ignore("Does not work with Scala FEEL Engine anymore, "
      + "Philipp says it's not standard conform with DMN 1.1 "
      + "to use a built-in function in this input cell and to expect"
      + "a unary test, you should rather write '? = date and time(dateString)'"
      + "to indicate a unary test expression")
  @Test
  @DecisionResource(resource = DATE_AND_TIME_DMN)
  public void testDateAndTimeIntegration() {
    Date testDate = new Date(1445526087000L);
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    variables.putValue("dateString", format.format(testDate));

    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.dateValue(testDate));
  }

  @Ignore("just a verification that 'equal-to' unary tests on built-in functions "
      + "don't work with Scala FEEL engine in general")
  @Test
  @DecisionResource
  public void testUnaryTest() {
    variables.putValue("integerString", "45");

    assertThatDecisionTableResult()
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.integerValue(45));
  }

  @Test
  @DecisionResource(resource = DMN)
  public void testFeelInputExpression() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    configuration.setDefaultInputExpressionExpressionLanguage(DefaultDmnEngineConfiguration.FEEL_EXPRESSION_LANGUAGE);
    DmnEngine engine = configuration.buildEngine();

    variables.putValue("score", 3);

    assertThatDecisionTableResult(engine)
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.stringValue("a"));
  }

  @Test
  @DecisionResource(resource = DMN_12)
  public void testFeelInputExpression_Dmn12() {
    testFeelInputExpression();
  }

  @Test
  @DecisionResource(resource = DMN)
  public void testFeelOutputEntry() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    configuration.setDefaultOutputEntryExpressionLanguage(DefaultDmnEngineConfiguration.FEEL_EXPRESSION_LANGUAGE);
    DmnEngine engine = configuration.buildEngine();

    variables.putValue("score", 3);

    assertThatDecisionTableResult(engine)
      .hasSingleResult()
      .hasSingleEntryTyped(Variables.stringValue("a"));
  }

  @Test
  @DecisionResource(resource = DMN)
  public void testFeelInputExpressionWithCustomEngine() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    configuration.setDefaultInputExpressionExpressionLanguage(DefaultDmnEngineConfiguration.FEEL_EXPRESSION_LANGUAGE);
    DmnEngine engine = configuration.buildEngine();

    DmnDecisionResult decisionResult = engine.evaluateDecision(decision, Variables.createVariables().putValue("score", 3));

    assertThat((String)decisionResult.getSingleEntry()).isEqualTo("a");
  }

  @Test
  @DecisionResource(resource = DMN)
  public void testFeelOutputEntryWithCustomEngine() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    configuration.setDefaultOutputEntryExpressionLanguage(DefaultDmnEngineConfiguration.FEEL_EXPRESSION_LANGUAGE);
    DmnEngine engine = configuration.buildEngine();

    DmnDecisionResult decisionResult = engine.evaluateDecision(decision, Variables.createVariables().putValue("score", 3));

    assertThat((String)decisionResult.getSingleEntry()).isEqualTo("a");
  }

  @Ignore("No single quotes support")
  @Test
  @DecisionResource(resource = "FeelLegacy_SingleQuotes.dmn")
  public void shouldUseSingleQuotesInStringLiterals() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    DmnEngine engine = configuration.buildEngine();

    DmnDecisionResult decisionResult = engine.evaluateDecision(decision,
      Variables.createVariables().putValue("input", "Hello World"));

    assertThat((String)decisionResult.getSingleEntry()).isEqualTo("foo");
  }

  @Ignore("SPIN handling has changed")
  @Test
  @DecisionResource(resource = "FeelLegacy_SPIN.dmn")
  public void shouldHandleSpinCorrectly() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    DmnEngine engine = configuration.buildEngine();

    DmnDecisionResult decisionResult = engine.evaluateDecision(decision,
        Variables.createVariables()
            .putValue("foo", Spin.JSON("{ \"foo\": 7}")));

    assertThat((String)decisionResult.getSingleEntry()).isEqualTo("foo");
  }

  @Test
  @DecisionResource(resource = "FeelLegacy_SPIN_Context.dmn")
  public void shouldUseContextConversionForSpinValue() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    DmnEngine engine = configuration.buildEngine();

    DmnDecisionResult decisionResult = engine.evaluateDecision(decision,
        Variables.createVariables()
            .putValue("foo", Spin.JSON("{ \"bar\": 7}")));

    assertThat((String)decisionResult.getSingleEntry()).isEqualTo("bar");
  }

  @Ignore("No equals performed see 'testDateAndTimeIntegration'")
  @Test
  @DecisionResource(resource = "FeelLegacy_equals_boolean.dmn")
  public void shouldEqualBoolean() {
    DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) getDmnEngineConfiguration();
    DmnEngine engine = configuration.buildEngine();

    DmnDecisionResult decisionResult = engine.evaluateDecision(decision, Variables.createVariables());

    assertThat((String)decisionResult.getSingleEntry()).isEqualTo("foo");
  }

}
