#!/bin/bash
# AWS Setup Script for CI/CD Pipeline
# Creates: Jenkins Server EC2 + Docker Host EC2
# Aligned with Lecture #9 and #10

REGION="eu-west-1"
KEY_NAME="cpipeline-key"
SG_NAME="cpipeline-sg"

echo "=== Creating Key Pair ==="
aws ec2 create-key-pair \
    --key-name $KEY_NAME \
    --query 'KeyMaterial' \
    --output text \
    --region $REGION > ${KEY_NAME}.pem
chmod 400 ${KEY_NAME}.pem
echo "Key saved to ${KEY_NAME}.pem"

echo "=== Creating Security Group ==="
VPC_ID=$(aws ec2 describe-vpcs --filters "Name=isDefault,Values=true" --query 'Vpcs[0].VpcId' --output text --region $REGION)
SG_ID=$(aws ec2 create-security-group \
    --group-name $SG_NAME \
    --description "CI/CD Pipeline Security Group" \
    --vpc-id $VPC_ID \
    --query 'GroupId' \
    --output text \
    --region $REGION)

# Open ports: SSH(22), Jenkins(8080), SonarQube(9000), App(8081)
for PORT in 22 8080 9000 8081; do
    aws ec2 authorize-security-group-ingress \
        --group-id $SG_ID \
        --protocol tcp \
        --port $PORT \
        --cidr 0.0.0.0/0 \
        --region $REGION
done
echo "Security Group: $SG_ID"

# Amazon Linux 2023 AMI (eu-west-1)
AMI_ID=$(aws ec2 describe-images \
    --owners amazon \
    --filters "Name=name,Values=al2023-ami-2023*-x86_64" "Name=state,Values=available" \
    --query 'Images | sort_by(@, &CreationDate) | [-1].ImageId' \
    --output text \
    --region $REGION)

echo "=== Launching Jenkins Server ==="
JENKINS_ID=$(aws ec2 run-instances \
    --image-id $AMI_ID \
    --instance-type t2.medium \
    --key-name $KEY_NAME \
    --security-group-ids $SG_ID \
    --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=Jenkins-Server}]' \
    --user-data '#!/bin/bash
sudo yum update -y
sudo yum install -y java-17-amazon-corretto git maven
sudo wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
sudo rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io-2023.key
sudo yum install -y jenkins
sudo systemctl enable jenkins
sudo systemctl start jenkins
# Install Ansible
sudo yum install -y ansible
# Install Docker (for SonarQube)
sudo yum install -y docker
sudo systemctl enable docker
sudo systemctl start docker
sudo docker run -d --name sonarqube -p 9000:9000 sonarqube:lts-community' \
    --query 'Instances[0].InstanceId' \
    --output text \
    --region $REGION)
echo "Jenkins Server: $JENKINS_ID"

echo "=== Launching Docker Host ==="
DOCKER_ID=$(aws ec2 run-instances \
    --image-id $AMI_ID \
    --instance-type t2.micro \
    --key-name $KEY_NAME \
    --security-group-ids $SG_ID \
    --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=Docker-Host}]' \
    --user-data '#!/bin/bash
sudo yum update -y
sudo yum install -y docker
sudo systemctl enable docker
sudo systemctl start docker
sudo useradd ansadmin
echo "ansadmin:ansadmin" | sudo chpasswd
sudo usermod -aG docker ansadmin
sudo sed -i "s/PasswordAuthentication no/PasswordAuthentication yes/" /etc/ssh/sshd_config
sudo systemctl restart sshd
sudo mkdir -p /opt/docker
sudo chown ansadmin:ansadmin /opt/docker' \
    --query 'Instances[0].InstanceId' \
    --output text \
    --region $REGION)
echo "Docker Host: $DOCKER_ID"

echo "=== Waiting for instances to start ==="
aws ec2 wait instance-running --instance-ids $JENKINS_ID $DOCKER_ID --region $REGION

JENKINS_IP=$(aws ec2 describe-instances --instance-ids $JENKINS_ID --query 'Reservations[0].Instances[0].PublicIpAddress' --output text --region $REGION)
DOCKER_IP=$(aws ec2 describe-instances --instance-ids $DOCKER_ID --query 'Reservations[0].Instances[0].PublicIpAddress' --output text --region $REGION)

echo ""
echo "========================================="
echo "  Setup Complete!"
echo "========================================="
echo "Jenkins Server: http://${JENKINS_IP}:8080"
echo "SonarQube:      http://${JENKINS_IP}:9000"
echo "Docker Host:    ${DOCKER_IP}"
echo ""
echo "SSH into Jenkins: ssh -i ${KEY_NAME}.pem ec2-user@${JENKINS_IP}"
echo "SSH into Docker:  ssh -i ${KEY_NAME}.pem ec2-user@${DOCKER_IP}"
echo ""
echo "=== Next Steps ==="
echo "1. Wait ~3 mins for Jenkins to start"
echo "2. Get Jenkins password: sudo cat /var/lib/jenkins/secrets/initialAdminPassword"
echo "3. Install plugins: Publish Over SSH, Pipeline, Maven, SonarQube Scanner"
echo "4. SonarQube default login: admin/admin"
echo "5. Update ansible/hosts with Docker Host IP: ${DOCKER_IP}"
echo "6. Update Jenkinsfile placeholders with your GitHub repo URL"
echo "7. Configure 'Publish over SSH' in Jenkins with ansible-server connection"
echo "========================================="
