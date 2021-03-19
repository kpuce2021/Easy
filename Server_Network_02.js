var mysql = require('mysql');
var express = require('express');
var bodyParser = require('body-parser');
var app = express();

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

app.listen(3000,function () { //파라미터 2개만 넘김
    console.log('server running...');
});

var connection = mysql.createConnection({
    host: "DB dns",
    user: "user",
    database: "loginSample",
    password: "pwd",
    port: 3306
});

app.post('/user/join', function (req, res) {
    console.log(req.body);
    var userEmail = req.body.userEmail;
    var userPwd = req.body.userPwd;
    var userName = req.body.userName;

    var sql = 'INSERT INTO Users (UserEmail, UserPwd, UserName) VALUES (?, ?, ?)';
    var params = [userEmail, userPwd, userName];

    connection.query(sql, params, function (err, result) {
        var resultCode = 404;
        var message = 'error has occured';

        if (err) {
            console.log(err);
        } else {
            resultCode = 200;
            message = 'succeed register.';
        }

        res.json({
            'code': resultCode,
            'message': message
        });
    });
});

app.post('/user/login', function (req, res) {
    var userEmail = req.body.userEmail;
    var userPwd = req.body.userPwd;
    var sql = 'select * from Users where UserEmail = ?';

    connection.query(sql, userEmail, function (err, result) {
        var resultCode = 404;
        var message = 'error has occured';

        if (err) {
            console.log(err);
        } else {
            if (result.length === 0) {
                resultCode = 204;
                message = 'Non-exist account!';
            } else if (userPwd !== result[0].UserPwd) {
                resultCode = 204;
                message = 'Wrong pwd';
            } else {
                resultCode = 200;
                message = 'succesful' + result[0].UserName + 'welcome!';
            }
        }

        res.json({
            'code': resultCode,
            'message': message
        });
    })
});
