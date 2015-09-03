var express = require('express');
var app = express();

app.set('port', (process.env.PORT || 5000));

app.use(express.static(__dirname + '/public'));

// views is directory for all template files
app.set('views', __dirname + '/views');
app.set('view engine', 'ejs');

app.get('/', function(request, response) {
  response.render('pages/index');
});

app.get('/master', function(request, res) {
    res.setHeader('Content-Type', 'application/json');
    res.send(JSON.stringify({ "master_link": "http://10.44.193.144:5000/master", "title":"Dublin Real Time", "image":"http://10.44.193.144:5000/images/bus.jpg" }));
});

app.listen(app.get('port'), function() {
  console.log('Node app is running on port', app.get('port'));
});
