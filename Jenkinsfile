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
                    --env-file /home/ubuntu/payment-service.env \
                    ${ECR_REPOSITORY}:${IMAGE_TAG}


                    echo "================================="
                    echo "CONTAINER STATUS"
                    echo "================================="

                    docker ps \
                    --filter "name=${CONTAINER_NAME}"


                    echo "================================="
                    echo "HEALTH CHECK"
                    echo "================================="

                    sleep 10

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
