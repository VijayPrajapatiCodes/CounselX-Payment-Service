pipeline {

    agent any

    environment {

        // =========================
        // AWS / ECR
        // =========================
        AWS_REGION = 'ap-south-1'

        ECR_REGISTRY =
            '196893792695.dkr.ecr.ap-south-1.amazonaws.com'

        ECR_REPOSITORY =
            '196893792695.dkr.ecr.ap-south-1.amazonaws.com/counselx/payment-service'

        // =========================
        // Docker Image
        // =========================
        IMAGE_TAG = "${BUILD_NUMBER}"

        // =========================
        // PAYMENT SERVICE EC2
        // =========================
        EC2_HOST = '13.127.213.206'
        EC2_USER = 'ubuntu'

        CONTAINER_NAME = 'payment-service'
        CONTAINER_PORT = '8082'
    }

    stages {

        // ==========================================
        // 1. Verify Environment
        // ==========================================
        stage('Verify Environment') {
            steps {
                echo 'Checking Jenkins environment...'

                sh '''
                    echo "Java:"
                    java -version

                    echo ""
                    echo "Maven:"
                    mvn -version

                    echo ""
                    echo "Docker:"
                    docker --version

                    echo ""
                    echo "AWS CLI:"
                    aws --version
                '''
            }
        }

        // ==========================================
        // 2. Maven Build
        // ==========================================
        stage('Maven Build') {
            steps {
                echo 'Building Spring Boot application...'

                sh '''
                    mvn clean package -DskipTests
                '''
            }
        }

        // ==========================================
        // 3. Build OCI Image using Buildpacks
        // ==========================================
        stage('Build Image with Buildpacks') {
            steps {
                echo 'Building OCI image using Spring Boot Buildpacks...'

                sh """
                    mvn spring-boot:build-image \
                    -Dspring-boot.build-image.imageName=${ECR_REPOSITORY}:${IMAGE_TAG}
                """
            }
        }

        // ==========================================
        // 4. Login to AWS ECR
        // ==========================================
        stage('Login to ECR') {
            steps {
                echo 'Logging in to AWS ECR...'

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

        // ==========================================
        // 5. Push Image to ECR
        // ==========================================
        stage('Push Image to ECR') {
            steps {

                echo "Pushing image: ${ECR_REPOSITORY}:${IMAGE_TAG}"

                sh """
                    docker push ${ECR_REPOSITORY}:${IMAGE_TAG}
                """

                echo 'Updating latest tag...'

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

        // ==========================================
        // 6. Deploy to Payment EC2
        // ==========================================
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
                            echo "PULLING NEW IMAGE"
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
                            ${ECR_REPOSITORY}:${IMAGE_TAG}


                            echo "================================="
                            echo "CONTAINER STATUS"
                            echo "================================="

                            docker ps \
                            --filter "name=${CONTAINER_NAME}"


                            echo "================================="
                            echo "DEPLOYMENT COMPLETED"
                            echo "================================="

REMOTE_COMMANDS
                    """
                }
            }
        }
    }

    // ==========================================
    // POST ACTIONS
    // ==========================================
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
