pipeline {

    agent any

    environment {

        AWS_REGION = 'ap-south-1'

        ECR_REGISTRY =
            '196893792695.dkr.ecr.ap-south-1.amazonaws.com'

        ECR_REPOSITORY =
            '196893792695.dkr.ecr.ap-south-1.amazonaws.com/counselx/payment-service'

        IMAGE_TAG = "${BUILD_NUMBER}"

        EC2_HOST = '13.127.213.206'
        EC2_USER = 'ubuntu'

        CONTAINER_NAME = 'payment-service'
        CONTAINER_PORT = '8082'
    }

    stages {

        stage('Verify Environment') {
            steps {
                echo 'Checking Jenkins environment...'

                sh '''
                    java -version
                    mvn -version
                    docker --version
                    aws --version
                '''
            }
        }

        stage('Maven Build') {
            steps {
                echo 'Building Spring Boot application...'

                sh '''
                    mvn clean package -DskipTests
                '''
            }
        }

        stage('Build Image with Buildpacks') {
            steps {
                echo 'Building OCI image...'

                sh """
                    mvn spring-boot:build-image \
                    -Dspring-boot.build-image.imageName=${ECR_REPOSITORY}:${IMAGE_TAG}
                """
            }
        }

        stage('Login to ECR') {
            steps {
                echo 'Logging in to ECR...'

                sh """
                    aws ecr get-login-password \
                    --region ${AWS_REGION} \
                    | docker login \
                    --username AWS \
                    --password-stdin \
                    ${ECR_REGISTRY}
                """
            }
        }

        stage('Push Image to ECR') {
            steps {

                echo "Pushing ${ECR_REPOSITORY}:${IMAGE_TAG}"

                sh """
                    docker push ${ECR_REPOSITORY}:${IMAGE_TAG}
                """

                sh """
                    docker tag \
                    ${ECR_REPOSITORY}:${IMAGE_TAG} \
                    ${ECR_REPOSITORY}:latest
                """

                sh """
                    docker push \
                    ${ECR_REPOSITORY}:latest
                """
            }
        }

        stage('Deploy to Payment EC2') {
            steps {

                echo 'Deploying Payment Service to EC2...'

                sshagent(credentials: ['payment-ec2-ssh-key']) {

                    sh """
                        ssh -o StrictHostKeyChecking=no \
                        ${EC2_USER}@${EC2_HOST} << 'REMOTE_COMMANDS'

                        set -e

                        echo "================================="
                        echo "AWS ECR LOGIN"
                        echo "================================="

                        aws ecr get-login-password \
                        --region ${AWS_REGION} \
                        | docker login \
                        --username AWS \
                        --password-stdin \
                        ${ECR_REGISTRY}

                        echo "================================="
                        echo "PULLING IMAGE"
                        echo "================================="

                        docker pull \
                        ${ECR_REPOSITORY}:${IMAGE_TAG}

                        echo "================================="
                        echo "STOPPING OLD CONTAINER"
                        echo "================================="

                        docker stop ${CONTAINER_NAME} || true

                        echo "================================="
                        echo "REMOVING OLD CONTAINER"
                        echo "================================="

                        docker rm ${CONTAINER_NAME} || true

                        echo "================================="
                        echo "STARTING NEW CONTAINER"
                        echo "================================="

                        docker run -d \
                        --name ${CONTAINER_NAME} \
                        --restart unless-stopped \
                        -p ${CONTAINER_PORT}:${CONTAINER_PORT} \
                        --env-file /home/ubuntu/payment-service.env \
                        ${ECR_REPOSITORY}:${IMAGE_TAG}

                        echo "================================="
                        echo "CONTAINER STATUS"
                        echo "================================="

                        docker ps \
                        --filter "name=${CONTAINER_NAME}"

                        echo "================================="
                        echo "WAITING FOR APPLICATION"
                        echo "================================="

                        sleep 10

                        echo "================================="
                        echo "HEALTH CHECK"
                        echo "================================="

                        curl -f \
                        http://127.0.0.1:${CONTAINER_PORT}/actuator/health

                        echo ""
                        echo "================================="
                        echo "DEPLOYMENT COMPLETED"
                        echo "================================="

REMOTE_COMMANDS
                    """
                }
            }
        }
    }

    post {

        success {
            echo '''
            =========================================
            PAYMENT SERVICE DEPLOYMENT SUCCESSFUL
            =========================================
            '''
        }

        failure {
            echo '''
            =========================================
            PAYMENT SERVICE DEPLOYMENT FAILED
            =========================================
            '''
        }

        always {
            echo "========================================="
            echo "Jenkins Build : ${BUILD_NUMBER}"
            echo "ECR Image     : ${ECR_REPOSITORY}:${IMAGE_TAG}"
            echo "========================================="
        }
    }
}
