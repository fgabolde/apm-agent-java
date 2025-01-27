/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.apm.agent.report;

import co.elastic.apm.agent.impl.metadata.MetaData;
import co.elastic.apm.agent.impl.stacktrace.StacktraceConfiguration;
import co.elastic.apm.agent.report.processor.ProcessorEventHandler;
import co.elastic.apm.agent.report.serialize.DslJsonSerializer;
import org.stagemonitor.configuration.ConfigurationRegistry;

import javax.annotation.Nonnull;
import java.util.concurrent.Future;

public class ReporterFactory {

    public Reporter createReporter(ConfigurationRegistry configurationRegistry,
                                   ApmServerClient apmServerClient,
                                   Future<MetaData> metaData,
                                   ReporterMonitor monitor) {

        ReporterConfiguration reporterConfiguration = configurationRegistry.getConfig(ReporterConfiguration.class);
        ReportingEventHandler reportingEventHandler = getReportingEventHandler(configurationRegistry, reporterConfiguration, metaData, apmServerClient);
        return new ApmServerReporter(true, reporterConfiguration, reportingEventHandler, monitor);
    }

    @Nonnull
    private ReportingEventHandler getReportingEventHandler(ConfigurationRegistry configurationRegistry,
                                                           ReporterConfiguration reporterConfiguration,
                                                           Future<MetaData> metaData,
                                                           ApmServerClient apmServerClient) {

        DslJsonSerializer payloadSerializer = new DslJsonSerializer(configurationRegistry.getConfig(StacktraceConfiguration.class), apmServerClient, metaData);
        ProcessorEventHandler processorEventHandler = ProcessorEventHandler.loadProcessors(configurationRegistry);
        return new IntakeV2ReportingEventHandler(reporterConfiguration, processorEventHandler, payloadSerializer, apmServerClient);
    }

}
