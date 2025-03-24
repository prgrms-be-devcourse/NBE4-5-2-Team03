# NBE4-5-2-Team03

## 프로젝트 실행

프로젝트 루트에 아래와 같이 `.env`파일을 작성한다.

```
MYSQL_ROOT_PASSWORD={MySQL 인스턴스의 root 계정 비밀번호}
MYSQL_DATABASE=flicktionary

DB_USERNAME=root
DB_PASSWORD={MySQL 인스턴스의 root 계정 비밀번호}
DB_URL=jdbc:mysql://mysql:3306/flicktionary
JWT_SECRET_KEY={JWT 토큰을 서명할 128비트 비밀키}
TMDB_ACCESS_TOKEN={TMDB API 인증 토큰}
TMDB_BASE_IMAGE_URL=localhost
EMAIL_USERNAME={지메일 SMTP서버 인증에 사용할 유저네임}
EMAIL_PASSWORD={지메일 SMTP서버 인증에 사용할 비밀번호}
EMAIL_SENDER_ADDRESS={메일 발송시 보낸이로 지정할 이메일 주소}
```

백엔드 서비스를 컴파일 한 뒤,

```
cd backend
./gradlew clean bootJar
```

Docker Compose로 BE와 DB를 실행한다.

```
cd ..
docker compose up
```

마지막으로 FE를 실행한다.
```
cd frontend
npm install --legacy-peer-deps
npm run start
```

