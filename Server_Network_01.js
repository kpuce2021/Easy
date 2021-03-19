const express = require('express');
const app = express();

const server = app.listen(3000, () => {
    console.log('Start Server : localhost:3000');
});

app.set('views',__dirname + '/views'); // html 파일을 불러오기위한 set함수 전처리
app.set('view engine','ejs'); // html안에서 javascript코드를 쓸수있게 해주는 npm 모듈
app.engine('html',require('ejs').renderFile);

// client(web-page)_request -- server(web-server)_response
//request 할 때마다 서버의 어떤 기능과 매핑을 할지 결정해주는 라우팅
//클라이언트의 요청대로 라우팅
app.get('/', function(req,res){ // get 방식으로 데이터 response , default값 / 
    //res.send('hello World')
    res.render('index.html') // html 페이지를 보내주는 함수
})

app.get('/about', function(req,res){ // get 방식으로 데이터 response , 라우터 /about 설정 
    //res.send('about page')
    res.render('about.html')
})

//mysql connection 설정
var mysql      = require('mysql');
var pool = mysql.createPool({
  connectionLimit : 10,
  host     : 'DB Dns',
  user     : 'user',
  password : 'pwd',
  database : 'loginSample'
});

//var pool  = mysql.createPool(...);

app.get('/db', function(req,res){ // get 방식으로 데이터 response , 라우터 /about 설정 
   
    //사용자가 /db 페이지를 request 하면 db connection을 이용해 페이지를띄움
    pool.getConnection(function(err, connection) {
        if (err) throw err; // not connected!
       
        // Use the connection
        connection.query('select * from Users', function (error, results, fields) { // Users 테이블에있는 모든 데이터 불러오기
          res.send(JSON.stringify(results));//화면에다 send로 가져온데이터를 보내줌
          console.log('results',results);

            // When done with the connection, release it.
          connection.release();
       
          // Handle error after the release.
          if (error) throw error;
       
          // Don't use the connection here, it has been returned to the pool.
        });
      });
})