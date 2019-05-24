var http = require("http");
var url = require("url");
var fs = require("fs");
var path = require("path");
var socketio = require("socket.io");

var MongoClient = require('mongodb').MongoClient;
var MongoServer = require('mongodb').Server;
var mimeTypes = { "html": "text/html", "jpeg": "image/jpeg", "jpg": "image/jpeg", "png": "image/png", "js": "text/javascript", "css": "text/css", "swf": "application/x-shockwave-flash"};


var luminosity = 0, minLuminosity = 0, maxLuminosity = 10;
var temperature = 0, minTemperature = -5, maxTemperature = 45;
//0 = 0% raised, 100 = 100% raised
var shutter = 100;
//0 - off, 1 - on
var air_conditioner = 0;
var luminosityWarning = false;
var temperatureWarning = false;
// false = auto, true = manual
var mode = false;
//0 - nobody, 1 - someone
var presence = 0;
//false = off, true = on
var tv = false;
var timeout = false;

//Server
var httpServer = http.createServer(
	function(request, response) {
		var uri = url.parse(request.url).pathname;
		if (uri=="/") uri = "/user.html";
		var fname = path.join(process.cwd(), uri);
		fs.exists(fname, function(exists) {
			if (exists) {
				fs.readFile(fname, function(err, data){
					if (!err) {
						var extension = path.extname(fname).split(".")[1];
						var mimeType = mimeTypes[extension];
						response.writeHead(200, mimeType);
						response.write(data);
						response.end();
					}
					else {
						response.writeHead(200, {"Content-Type": "text/plain"});
						response.write('Error de lectura en el fichero: '+uri);
						response.end();
					}
				});
			}
			else {
				console.log("Peticion invalida: "+uri);
				response.writeHead(200, {"Content-Type": "text/plain"});
				response.write('404 Not Found\n');
				response.end();
			}
		});
	}
);


httpServer.listen(8080);
var io = socketio.listen(httpServer);

//Agent
function agent() {
	if (luminosity > maxLuminosity) {
		io.sockets.emit('warningOn', 'maxluminosity');
		luminosityWarning = true;
	} else if (luminosity < minLuminosity) {
		io.sockets.emit('warningOn', 'maxluminosity');
		luminosityWarning = true;
	} else if (luminosityWarning) {
		io.sockets.emit('warningOff', 'luminosity');
		luminosityWarning = false;
	}

	if (temperature > maxTemperature) {
		io.sockets.emit('warningOn', 'maxtemperature');
		temperatureWarning = true;
	} else if (temperature < minTemperature) {
		io.sockets.emit('warningOn', 'mintemperature');
		temperatureWarning = true;
	} else if (temperatureWarning) {
		io.sockets.emit('warningOff', 'temperature');
		temperatureWarning = false;
	}

	//Lower the shutter if both limits are exceeded
	if (luminosityWarning && temperatureWarning) {
		io.sockets.emit('limitsExceeded', 'limitsExceeded');
		if (!mode) updateShutter(0);
	}

	//Turn on the TV or off if there is no presence in 60 seconds
	if (presence == 1) {
		updateTV(1);
		timeout = false;
	} else if (!timeout) setTimeout(function() {
		if (presence == 0) updateTV(0);
		timeout = true;
	}, 60000);

	//Turn on/off the air conditioner depending of the temperature
	if (!mode) {
		if (temperature > maxTemperature/2) updateAirConditioner(1);
		else updateAirConditioner(0);
	}
}

function updateSensors(lum, tem, pre) {
	luminosity = lum;
	temperature = tem;
	presence = pre;
	agent();
	io.sockets.emit('sensors', luminosity, temperature, presence);
}

function updateShutter(shut) {
	shutter = shut;
	io.sockets.emit('shutter', shutter);
}

function updateAirConditioner(air) {
	air_conditioner = air;
	io.sockets.emit('air_conditioner', air_conditioner);
}

function updateTV(t) {
	tv = t;
	io.sockets.emit('tv', tv);
}

function updateMode(mod) {
	mode = mod;
	io.sockets.emit('manual_auto', mode);
}

//DB
MongoClient.connect("mongodb://localhost:27017/", function(err, db) {
	if(!err){
		console.log("Conectado a Base de Datos");
	}

	var dbo = db.db("BaseDatos");
	var msgCliente = null;
	dbo.createCollection("Sensors", function(err, collection){
		if(!err){
			console.log("Colección creada en Mongo: " + collection.collectionName);
		}
	});

	io.sockets.on('connection',	function(client) {
		io.sockets.emit('sensors', luminosity, temperature, presence);
		io.sockets.emit('shutter', shutter);
		io.sockets.emit('air_conditioner', air_conditioner);

		client.on('sensors', function (luminosity, temperature, presence) {
			updateSensors(luminosity, temperature, presence);

			var date = Date(Date.now()).toString(); 
			dbo.collection("Sensors").insert({Date: date, Luminosity:luminosity, Temperature:temperature}, {safe:true}, function(err, result) {
				if(!err) {
					console.log("Inserted event at: "+ date + ", luminosity: " + luminosity + ", temperature: " + temperature + ", presence: " + presence);
				} else {
					console.log("Error al insertar datos en la colección.");
				}
			});	
		});
		client.on('shutter', function (shutter) {
			updateShutter(shutter);
		});
		client.on('air_conditioner', function (air_conditioner) {
			updateAirConditioner(air_conditioner);
		});
		client.on('tv', function (tv) {
			updateTV(tv);
		});
		client.on('manual_auto', function (mode) {
			updateMode(mode);
		});
	});
});

console.log("Socket.io ready");