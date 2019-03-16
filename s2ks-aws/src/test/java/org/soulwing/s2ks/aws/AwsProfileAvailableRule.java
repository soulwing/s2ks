/*
 * File created on Mar 16, 2019
 *
 * Copyright (c) 2019 Carl Harris, Jr
 * and others as noted
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
package org.soulwing.s2ks.aws;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A {@link TestRule} that ensures that the test environment contains the
 * required configuration needed to run tests for the AWS storage provider.
 *
 * @author Carl Harris
 */
public class AwsProfileAvailableRule implements TestRule {

  static final String KMS_MASTER_KEY = "KMS_MASTER_KEY";
  static final String S3_BUCKET_NAME = "S3_BUCKET_NAME";

  @Override
  public Statement apply(Statement statement, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        if ((System.getenv("AWS_PROFILE") == null
            && System.getProperty("aws.profile") == null)
            || System.getenv(KMS_MASTER_KEY) == null
            || System.getenv(S3_BUCKET_NAME) == null) {
          throw new AssumptionViolatedException(
              "required AWS storage provider configuration not available");
        }
        statement.evaluate();
      }
    };
  }

}
