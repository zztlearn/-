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

import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.test.DecisionResource;
import org.camunda.bpm.dmn.engine.test.DmnEngineTest;
import org.camunda.bpm.dmn.feel.impl.FeelEngine;
import org.camunda.bpm.dmn.feel.impl.juel.FeelEngineFactoryImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.feel.integration.CamundaFeelEngine;
import org.camunda.feel.integration.CamundaFeelEngineFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class ScalaFeelDateTest extends DmnEngineTest {

  protected static final String DATE_TABLE_INPUT_CLAUSE_TYPE_DATE_CONVERSION =
      "DateConversionTable_InputClauseTypeDate.dmn11.dmn";

  protected static final String DATE_TABLE_INPUT_CLAUSE_TYPE_NON_CONVERSION =
      "DateConversionTable_NonInputClauseType.dmn11.dmn";

  protected FeelEngine scalaFeelEngine;
  protected FeelEngine javaFeelEngine;

  @Override
  public DmnEngineConfiguration getDmnEngineConfiguration() {
    DefaultDmnEngineConfiguration configuration = new DefaultDmnEngineConfiguration();
    configuration.setFeelEngineFactory(new CamundaFeelEngineFactory());
    return configuration;
  }

  @Before
  public void assignEngines() {
    javaFeelEngine = new FeelEngineFactoryImpl()
        .createInstance();

    scalaFeelEngine = new CamundaFeelEngine();
  }

  @Test
  @DecisionResource(resource = "DateConversionLit.dmn11.xml")
  public void shouldEvaltuateToUtilDateWithLiteralExpression() {
    // given
    getVariables()
        .putValue("date1", new Date());

    // when
    Object foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isInstanceOf(Date.class);
  }

  @Test
  @DecisionResource(resource = DATE_TABLE_INPUT_CLAUSE_TYPE_NON_CONVERSION)
  public void shouldEvaluateLocalDateWithTable_NonInputClauseType() {
    // given
    getVariables()
        .putValue("date1", LocalDateTime.now());

    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("not ok");
  }

  @Ignore("scala engine adds timezone")
  @Test
  @DecisionResource(resource = DATE_TABLE_INPUT_CLAUSE_TYPE_NON_CONVERSION)
  public void shouldEvaluateUtilDateWithTable_NonInputClauseType() {
    // given
    getVariables()
        .putValue("date1", new Date());

    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("not ok");
  }

  @Test
  @DecisionResource(resource = DATE_TABLE_INPUT_CLAUSE_TYPE_NON_CONVERSION)
  public void shouldEvaluateJodaDateWithTable_NonInputClauseType() {
    // given
    getVariables()
        .putValue("date1", org.joda.time.LocalDate.now());

    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("not ok");
  }

  @Test
  public void shouldEvaluateLocalDateWithJavaEngine_InputClauseTypeDate() {
    // given
    VariableMap variables = getVariables()
        .putValue("date1", LocalDateTime.now());

    // when
    boolean result = javaFeelEngine.evaluateSimpleUnaryTests("<=date and time(\"2014-11-30T12:00:00\")",
        "date1", variables.asVariableContext());

    // then
    assertThat(result).isTrue();
  }

  @Test
  public void shouldEvaluateLocalDateWithScalaEngine_InputClauseTypeDate() {
    // given
    VariableMap variables = getVariables()
        .putValue("date1", LocalDateTime.now());

    // when
    boolean result = scalaFeelEngine.evaluateSimpleUnaryTests("<=date and time(\"2014-11-30T12:00:00\")",
        "date1", variables.asVariableContext());

    // then
    assertThat(result).isFalse();
  }

  @Test
  public void shouldEvaluateJodaLocalDateWithJavaEngine_InputClauseTypeDate() {
    // given
    VariableMap variables = getVariables()
        .putValue("date1", org.joda.time.LocalDateTime.now());

    // when
    boolean result = javaFeelEngine.evaluateSimpleUnaryTests("<=date and time(\"2014-11-30T12:00:00\")",
        "date1", variables.asVariableContext());

    // then
    assertThat(result).isTrue();
  }

  @Test
  public void shouldEvaluateJodaLocalDateWithScalaEngine_InputClauseTypeDate() {
    // given
    VariableMap variables = getVariables()
        .putValue("date1", org.joda.time.LocalDateTime.now());

    // when
    boolean result = scalaFeelEngine.evaluateSimpleUnaryTests("<=date and time(\"2014-11-30T12:00:00\")",
        "date1", variables.asVariableContext());

    // then
    assertThat(result).isFalse();
  }

  @Test
  public void shouldEvaluateUtilDateWithJavaEngine_InputClauseTypeDate() {
    // given
    VariableMap variables = getVariables()
        .putValue("date1", new Date());

    // when
    boolean result = javaFeelEngine.evaluateSimpleUnaryTests("<=date and time(\"2014-11-30T12:00:00\")",
        "date1", variables.asVariableContext());

    // then
    assertThat(result).isFalse();
  }

  @Ignore("scala engine adds timezone")
  @Test
  public void shouldEvaluateUtilDateWithScalaEngine_InputClauseTypeDate() {
    // given
    VariableMap variables = getVariables()
        .putValue("date1", new Date());

    // when
    boolean result = scalaFeelEngine.evaluateSimpleUnaryTests("<=date and time(\"2014-11-30T12:00:00\")",
        "date1", variables.asVariableContext());

    // then
    assertThat(result).isFalse();
  }

  @Ignore("scala engine adds timezone")
  @Test
  @DecisionResource(resource = DATE_TABLE_INPUT_CLAUSE_TYPE_DATE_CONVERSION)
  public void shouldEvaluateUtilDateWithTable_InputClauseTypeDate() {
    // given
    getVariables()
        .putValue("date1", new Date());

    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("not ok");
  }

  @Test
  @DecisionResource(resource = DATE_TABLE_INPUT_CLAUSE_TYPE_DATE_CONVERSION)
  public void shouldEvaluateJodaDateWithTable_InputClauseTypeDate() {
    // given
    getVariables()
        .putValue("date1", org.joda.time.LocalDate.now());

    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("not ok");
  }

  @Ignore("DMN engine converts to util date and scala engine adds timezone")
  @Test
  @DecisionResource(resource = DATE_TABLE_INPUT_CLAUSE_TYPE_DATE_CONVERSION)
  public void shouldEvaluateStringDateWithTable_InputClauseTypeDate() {
    // given
    getVariables()
        .putValue("date1", "2019-08-22T22:22:22");

    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("not ok");
  }

  @Test
  @DecisionResource(resource = "DateConversionTable_OutputTypeDate.dmn11.dmn")
  public void shouldOutputDateWithTable_OutputClauseTypeDate() {
    // given
    getVariables()
        .putValue("string1", "ok");

    // when
    Date foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo(new Date(1_543_575_600_000L));
  }

  @Test
  @DecisionResource(resource = "DateConversionTable_OutputFromVariableTypeDate.dmn11.dmn")
  public void shouldOutputDateResolveVariableWithTable_OutputClauseTypeDate() {
    // given
    Date date1 = new Date();
    getVariables()
        .putValue("string1", "ok")
        .putValue("date1", date1);

    // when
    Date foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo(date1);
  }

  @Test
  @DecisionResource(resource = "DateConversionTable_InputExpression.dmn11.dmn")
  public void shouldEvaluateInputExpression() {
    // given
    getVariables()
        .putValue("date1", new Date())
        .putValue("date2", new Date());
    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("bar");
  }

  @Test
  @DecisionResource(resource = "DateConversionTable_InputExpressionDateAndTime.dmn11.dmn")
  public void shouldEvaluateInputExpression2() {
    // given
    getVariables()
        .putValue("date1", new Date());


    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("bar");
  }

  @Test
  @DecisionResource(resource = "DateConversionTable_InputClausePerson.dmn11.dmn")
  public void shouldEvaluateInputClause_Object() {
    // given
    getVariables()
        .putValue("date1", new Date())
        .putValue("person", new Person(new Date()));

    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("bar");
  }

  /**
   * Fails for FEEL because Java 8 Date is not supported by the DMN engine.
   */
  @Test
  @DecisionResource(resource = "DateConversionTable_OutputClauseDateBuiltinFunction.dmn11.dmn")
  public void shouldEvaluateOutputClause() {
    // given

    // when
    String foo = evaluateDecision().getSingleEntry();

    // then
    assertThat(foo).isEqualTo("bar");
  }

  @Test
  public void shouldEvaluateDateAndTime_Java() throws ParseException {
    Date dateTime = new SimpleDateFormat("YYYY-MM-DD'T'HH:MM:ss")
        .parse("2015-12-12T22:12:53");

    getVariables()
        .putValue("input", dateTime);

    boolean input = javaFeelEngine.evaluateSimpleUnaryTests("date and time(\"2015-12-12T22:12:53\")",
        "input", getVariables().asVariableContext());

    assertThat(input).isFalse();
  }

  @Ignore("Scala engine add timezone information to util date")
  @Test
  public void shouldEvaluateDateAndTime_Scala() throws ParseException {
    Date dateTime = new SimpleDateFormat("YYYY-MM-DD'T'HH:MM:ss")
        .parse("2015-12-12T22:12:53");

    getVariables()
        .putValue("input", dateTime);

    boolean input = scalaFeelEngine.evaluateSimpleUnaryTests("date and time(\"2015-12-12T22:12:53\")",
        "input", getVariables().asVariableContext());

    assertThat(input).isFalse();
  }

  public class Person {

    Date birthday;

    public Person(Date age) {
      this.birthday = age;
    }

    public Date getBirthday() {
      return birthday;
    }

  }

}
