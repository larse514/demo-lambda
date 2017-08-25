#!groovy

@NonCPS
def renderTemplate(input, binding) {
    def engine = new groovy.text.SimpleTemplateEngine()
    def template = engine.createTemplate(input).make(binding)
    def temp_string = template.toString()

    engine = null
    template = null

    return temp_string
}

def sendEmail(body, subject){
    mail body: body,
            from: 'project',
            replyTo: 'andrew.larse514@gmail.com',
            subject: subject,
            to: 'andrew.larse514@gmail.com'
}

env.APP_VERSION = '1.0.'+env.BUILD_NUMBER
env.GRADLE_USER_HOME = '~./.gradle'
env.BUILD
env.DEPLOYMENT_ENVIRONMENT = '${ENVIRONMENT}'
def err
def status = 'Success'

def defaultStageName = 'latest'
def apiGatewayRef
def names
def currentDeploymentId
def functions = []

try {
    node('slave') {

        checkout scm

        stage('Build Test') {
            sh 'chmod +x ./gradlew'
            sh './gradlew clean build'
        }

        stage('Package') {
            sh 'aws cloudformation package \
            --template-file formation_assets.yaml \
            --output-template-file formation_assets_deploy.yaml \
            --s3-bucket lambdadeployables'
        }

        stage('Deploy Assets') {
            sh "aws cloudformation deploy \
            --template-file formation_assets_deploy.yaml \
            --capabilities CAPABILITY_IAM \
            --stack-name demo-assets\
            --parameter-overrides StageName=${defaultStageName}"

            sh 'aws cloudformation describe-stacks \
            --stack-name demo-assets \
            --query "Stacks[0].[Outputs[?! starts_with(OutputValue, \'arn:aws:lambda\')]][0][*].{OutputValue:OutputValue}" \
            --output text > apigatewayref'

            apiGatewayRef = readFile('apigatewayref').trim()

            sh 'aws cloudformation describe-stacks \
            --stack-name demo-assets \
            --query "Stacks[0].[Outputs[?starts_with(OutputValue, \'arn:aws:lambda\')]][0][*].{OutputValue:OutputValue}" \
            --output text > names'

            names = readFile 'names'

            sh "aws apigateway get-stages \
            --rest-api-id ${apiGatewayRef} \
            --query \"[item[?stageName=='${defaultStageName}']][0][0].deploymentId\" \
            --output text > deploymentId"

            currentDeploymentId = readFile('deploymentId').trim()

            sh "aws apigateway get-deployment \
            --rest-api-id ${apiGatewayRef} \
            --deployment-id ${currentDeploymentId} \
            --query \"description\" \
            --output text > deploymentDescription"

            currentBuild.description = readFile('deploymentDescription').trim()
        }

        stage('Publish Version') {
            for (def name : names.tokenize('\n')) {
                println "About to publish ${name} with description version ${env.APP_VERSION}"
                sh "aws lambda publish-version \
                --function-name ${name} \
                --description ${env.APP_VERSION} \
                --query '{Version:Version}' \
                --output text > version"

                version = readFile('version').trim()
                println "version ${version} created for function ${name}"
                functions << [name: name, version: version]
            }
        }

        stage('Update UAT') {
            env_template = readFile 'formation_env.yaml'
            println "current template ${env_template}"
            def output = renderTemplate(env_template, [functions: functions])
            println "new template is ${output}"
            writeFile file: 'formation_env_deploy.yaml', text: output
            sh "aws cloudformation deploy \
            --template-file formation_env_deploy.yaml \
            --stack-name demo-uat-environment \
            --parameter-overrides ApiGateway=${apiGatewayRef} StageName=uat DeploymentId=${currentDeploymentId} Version=${env.APP_VERSION}"
        }

        //send an email that the deployment to v1 will require an approval
        def body = "Deployment approval required for build ${env.BUILD_URL}"
        def subject = "Deployment approval required for ${env.BUILD_TAG}"
        sendEmail(body, subject)
    }

    //Execute this step via a flyweight thread.  This will not block the executors
    stage('v1 deployment approval') {
        //this can be configured to timout via a timeout tag:  timeout(time: 30, unit: 'MINUTES') {
        input message: 'Approve Deployment?'
    }

    //resume execution on an executor thread running on the slave node
    node('slave') {

        stage('Update v1') {
            sh "aws cloudformation deploy \
            --template-file formation_env_deploy.yaml \
            --stack-name demo-v1-environment \
            --parameter-overrides ApiGateway=${apiGatewayRef} StageName=v1 DeploymentId=${currentDeploymentId} Version=${env.APP_VERSION}"
        }
    }
}

catch(error){
    err = error
    status = 'Failed'
}

finally{
    def body = (err) ? 'Error : ' + err : 'Success' + '\r\n\r\n' + env.BUILD_URL
    def subject =  env.BUILD_TAG + " " + status
    sendEmail(body ,subject)
    node('slave') {
        deleteDir()
        if (err) {
            throw err
        }
    }
}
