var express = require('express');
var app = express();

app.set('port', 9000);

app.use(express.static(__dirname + '/public'));

// views is directory for all template files
app.set('views', __dirname + '/views');
app.set('view engine', 'ejs');

app.get('/', function(request, response) {
  response.render('pages/index');
});

app.get('/master', function(request, res) {
    res.setHeader('Content-Type', 'application/json');
    res.send(JSON.stringify({ "master_link": "http://ec2-52-11-75-142.us-west-2.compute.amazonaws.com:9000/", "title":"Dublin Real Time", "image":"http://ec2-52-11-75-142.us-west-2.compute.amazonaws.com:9000/images/bus.jpg" }));
});

app.listen(app.get('port'), function() {
  console.log('Node app is running on port', app.get('port'));
});
