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
    res.send(JSON.stringify({ "master_link": "https://vast-falls-7137.herokuapp.com", "title":"Dublin Real Time", "image":"https://vast-falls-7137.herokuapp.com/images/bus.jpg" }));
});

app.listen(app.get('port'), function() {
  console.log('Node app is running on port', app.get('port'));
});
