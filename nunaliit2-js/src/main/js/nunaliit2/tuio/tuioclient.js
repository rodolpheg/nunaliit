// Socket to tuioserver.js that emits TUIO events in JSON
var socket = io('http://localhost:3000');

// Speed factor for drag scrolling
var scrollSpeed = 1.0;

/** Construct a new cursor (finger). */
function Cursor() {
	this.x = Number.NaN;
	this.y = Number.NaN;
	this.down = false;
	this.downX = Number.Nan;
	this.downY = Number.Nan;
}

/** Construct a new tangible (block). */
function Tangible() {
	this.id = new Number();
	this.x = new Number();
	this.y = new Number();
	this.angle = new Number();
}

// Global dictionaries of alive cursors and tangibles
var cursors = new Object();
var tangibles = new Object();

/** Dispatch a mouse event as a result of a cursor change.
 * eventType: mousedown, mousemove, mouseup, or click
 * x, y: 0..1 normalized TUIO cordinates
 */
function dispatchMouseEvent(eventType, x, y) {
	// Convert table coordinates to browser coordinates
	var winX = x * window.innerWidth;
	var winY = y * window.innerHeight;

	// Get the topmost DOM element at this position
	var el = document.elementFromPoint(winX, winY);
	//console.log(eventType + " at " + winX + "," + winY + ": " + el + " id: " + el.id);

	// Create synthetic mouse event of the given type
	var event = new MouseEvent(eventType, {
		'view': window,
		'bubbles': true,
		'cancelable': true,
		'clientX': winX,
		'clientY': winY,
		'button': 0
	});

	// Dispatch to element
	el.dispatchEvent(event);
}

/** Update the list of things that are alive.
 * dict: Dictionary of cursors or tangibles.
 * alive: Updated alive array.
 */
function updateAlive(dict, alive) {
	// Remove any dead objects
	for (var inst in dict) {
		if (!dict.hasOwnProperty(inst)) {
			continue; // Ignore prototypes
		}

		// Check if this instance is still alive
		var found = false;
		for (var i = alive.length - 1; i >= 0; i--) {
			if (inst == alive[i]) {
				found = true;
				break;
			}
		}

		// Instance is not alive, so delete from dict
		if (!found) {
			// Dispatch mouseup event
			dispatchMouseEvent('mouseup', dict[inst].x, dict[inst].y);

			// If the cursor is unmoved since mousedown, this is a click
			if (dict[inst].x == dict[inst].downX &&
				dict[inst].y == dict[inst].downY) {
				dispatchMouseEvent('click', dict[inst].x, dict[inst].y);
			}

			// Remove cursor from dictionary
			delete dict[inst];
		}
	}

	// Add newly alive objects
	for (var i = alive.length - 1; i >= 0; i--) {
		if (!dict.hasOwnProperty(alive[i])) {
			var a = alive[i];
			if (dict == cursors) {
				/* This is a mousedown, but we do not have a position here.
				   Instead mousedown is dispatched on the initial position
				   update after a cursor becomes alive. */
				dict[a] = new Cursor();
			} else if (dict == tangibles) {
				dict[a] = new Tangible();
			}
		}
	}
}

/** Update cursor coordinates according to a position update. */
function updateCursors(set) {
	for (var inst in set) {
		if (!set.hasOwnProperty(inst)) {
			continue; // Ignore prototypes
		}

		if (set[inst] != undefined && cursors[inst] != undefined) {
			var newX = set[inst][0];
			var newY = set[inst][1];

			if (!isNaN(newX) && !isNaN(newY)) {
				// Have a previous coordinate (and thus a delta), scroll map
				/*
				// Direct use of OpenLayers API.
				// Uncomment the moduleDisplay in index.js for this to work
				var mapSize = moduleDisplay.mapControl.map.getSize();
				var dx = (cursors[inst].x - newX);
				var dy = (cursors[inst].y - newY);
				moduleDisplay.mapControl.map.pan(dx * mapSize.w * scrollSpeed,
				                                 dy * mapSize.h * scrollSpeed,
				                                 { dragging: true });
				*/

				if (!cursors[inst].down) {
					// Initial position update: mousedown
					dispatchMouseEvent('mousedown', newX, newY);
					cursors[inst].down = true;
					cursors[inst].downX = newX;
					cursors[inst].downY = newY;
				} else {
					// Subsequent update (already down): mousemove
					dispatchMouseEvent('mousemove', newX, newY);
				}
			}

			// Update stored cursor position
			cursors[inst].x = newX;
			cursors[inst].y = newY;
			cursors[inst].down = true;
		}
	}
}

function updateTangibles(set) {
	for (var inst in set) {
		if (!set.hasOwnProperty(inst)) {
			continue; // Ignore prototypes
		}

		if (set[inst] != undefined && tangibles[inst] != undefined) {
			tangibles[inst]['id'] = set[inst][0];
			tangibles[inst]['x'] = set[inst][1];
			tangibles[inst]['y'] = set[inst][2];
			tangibles[inst]['angle'] = set[inst][3];
		}
	}
}

socket.on('cursor update', function(update) {
	updateAlive(cursors, update.alive);
	updateCursors(update.set);
});

socket.on('tangibles update', function(update) {
	updateAlive(tangibles, update.alive);
	updateTangibles(update.set);
});
