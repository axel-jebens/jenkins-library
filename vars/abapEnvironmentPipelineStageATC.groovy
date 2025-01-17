import groovy.transform.Field
import com.cloudbees.groovy.cps.NonCPS
import com.sap.piper.GenerateStageDocumentation
import groovy.transform.Field
import com.sap.piper.Utils
import com.sap.piper.ConfigurationHelper
import com.sap.piper.ConfigurationLoader

import static com.sap.piper.Prerequisites.checkScript

@Field String STEP_NAME = getClass().getName()
@Field Set GENERAL_CONFIG_KEYS = []
@Field STAGE_STEP_KEYS = [
    /** Creates Communication Arrangements for ABAP Environment instance via the cloud foundry command line interface */
    'cloudFoundryCreateServiceKey',
    /** Starts an ATC check run on the ABAP Environment instance */
    'abapEnvironmentRunATCCheck',
    /** Creates/Updates ATC System Configuration */
    'abapEnvironmentPushATCSystemConfig',
    /** Parameter for ATC System Configuration json */
    'atcSystemConfigFilePath',
    /** Parameter for host config */
    'host'
]
@Field Set STEP_CONFIG_KEYS = GENERAL_CONFIG_KEYS.plus(STAGE_STEP_KEYS)
@Field Set PARAMETER_KEYS = STEP_CONFIG_KEYS
/**
 * This stage runs the ATC Checks & create/update ATC System Configuration before in case File Location provided
 */
void call(Map parameters = [:]) {
    def script = checkScript(this, parameters) ?: this
    def stageName = parameters.stageName?:env.STAGE_NAME

    // load default & individual configuration
    Map config = ConfigurationHelper.newInstance(this)
        .loadStepDefaults([:], stageName)
        .mixin(ConfigurationLoader.defaultStageConfiguration(script, stageName))
        .mixinGeneralConfig(script.commonPipelineEnvironment, GENERAL_CONFIG_KEYS)
        .mixinStepConfig(script.commonPipelineEnvironment, STEP_CONFIG_KEYS)
        .mixinStageConfig(script.commonPipelineEnvironment, stageName, STEP_CONFIG_KEYS)
        .mixin(parameters, PARAMETER_KEYS)
        .use()

    piperStageWrapper (script: script, stageName: stageName, stashContent: [], stageLocking: false) {
        echo "AJ: Start of call"

        //cloudFoundryCreateServiceKey(script: parameters.script, CfServiceKeyName: 'sap_com_0901', CfServiceKeyConfig: `{"scenario_id":"SAP_COM_0901","type":"basic"}`, CfAsync: false)
        cloudFoundryCreateServiceKey(script: parameters.script, cfServiceKeyName: 'sap_com_0763', cfServiceKeyConfig: '{"scenario_id":"SAP_COM_0763","type":"basic"}', cfAsync: false)
        cloudFoundryCreateServiceKey(script: parameters.script, cfServiceKeyName: 'sap_com_0193', cfServiceKeyConfig: '{"scenario_id":"SAP_COM_0193","type":"basic"}', cfAsync: false)

        //if (!config.host) {
        //  echo "AJ: Enter IF for config.host"
        //  cloudFoundryCreateServiceKey script: parameters.script
        //}
        //if (config.atcSystemConfigFilePath) {
        //  echo "AJ: PushATCConfig"
        //  abapEnvironmentPushATCSystemConfig script: parameters.script
        //}
        //echo "AJ: Run ATC Check"
        abapEnvironmentRunATCCheck script: parameters.script
    }
}
