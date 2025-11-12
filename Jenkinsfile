pipeline{
    agent any
    tools{
        gradle 'Gradle_9.2'
        jdk 'jdk_21'
    }
    stages{
        stage('checkout'){
            steps{
                git branch: 'jenkins', url: 'https://github.com/shukyurkevin/crypto-web-socket.git'
            }
        }

        stage('Build'){
            steps{
                bat './gradlew clean build'
            }
        }

        stage('Run'){
            steps{
                bat './gradlew run'
            }
        }

    }
    post {
        always {
            echo 'Build finished.'
        }
        failure {
            echo 'Build failed!'
        }
        success {
            echo 'Build succeeded!'
        }
    }
}
