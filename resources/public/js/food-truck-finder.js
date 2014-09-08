(function() {
  "use strict";
  $(document).ready(function() {
    var map = L.map('map');
    var osmUrl = '//{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
    var osmAttrib = 'Map data &copy; OpenStreetMap contributors';
    var osm = new L.TileLayer(osmUrl, {maxZoom: 18, attribution: osmAttrib});
    map.setView(new L.LatLng(37.78, -122.42),13);
    map.addLayer(osm);

    var markerLayerGroup = L.layerGroup().addTo(map);
    map.on('click', function(e) {
      var url = "/foodtrucks?x=" + e.latlng.lng.toString() + "&y=" + e.latlng.lat.toString() + "&limit=10&status=APPROVED";
      $.ajax({
        url: url,
        success: function(data) {
          markerLayerGroup.clearLayers();
          data.forEach(function(ft) {
            var marker = L.marker([ft.y, ft.x]).addTo(map);
            marker.bindPopup('<b>' + ft.applicant + '</b><br>' + ft.foodItems);
            markerLayerGroup.addLayer(marker);
          });
        }
      });

    });
  });
})();
