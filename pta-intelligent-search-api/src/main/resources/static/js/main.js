
$(document).ready(function() {
	document.getElementById("pta-haku-input-search-text").focus();

	$('#pta-haku-input-container input').keypress(function (e) {
		if (e.which == 13) {
			updateSortUI(); // reset sort
			teeHaku();
		}
	});

	$('#pta-haku-input-container button').click(function() {
		updateSortUI(); // reset sort
		teeHaku();
	});
	
	const sortFields = [{
		value: 'score', text: 'Vakio'
	},{
		value: 'title', text: 'Otsikko'
	},{
		value: 'datestamp', text: 'Julkaisupäivä'
	}];
	
	function updateSortUI(currentSort) {
		currentSort = currentSort || {};
		
		var sortti = $('#pta-haku-input #pta-haku-sort');
	
		var ret = $('<div><div>').attr('id', 'sort');
		var fieldSelect = $('<select></select>').attr('id', 'pta-haku-sort-field');
		
		for (var i in sortFields) {
			var opt = $('<option></option>')
				.attr('value',sortFields[i].value)
				.text(sortFields[i].text);
			if (currentSort.field === sortFields[i].value) {
				opt.attr('selected',true);
			}
			fieldSelect.append(opt);
		}
		ret.append(fieldSelect);
		
		var order = $('<input type="checkbox"></input>')
			.attr('id', 'pta-haku-sort-order')
			.text('Nouseva');
		
		if (currentSort.sort !== 'desc') {
			order.attr('checked', true);
		}
		ret.append(order);
		
		// The value of redoLastSearch is switched, so we can't bind to it directly
		fieldSelect.on('change', function() { redoLastSearch(); });
		order.on('change', function() { redoLastSearch(); });

		sortti.empty();
		sortti.append(ret);
	}
	
	function readSortFromUI() {
		var field = $('#pta-haku-input #pta-haku-sort #pta-haku-sort-field').val();
		var value = $('#pta-haku-input #pta-haku-sort #pta-haku-sort-order').is(":checked");
		return [{
			field: field,
			order: value ? 'desc' : 'asc'
		}];
	}

	const lang = 'FI';
	var pageSize = 10;
	
	var redoLastSearch = function() { };

	function teeHaku(skip, facetQuery) {
		skip       = skip || 0;
		facetQuery = facetQuery || {};
		
		redoLastSearch = function() {
			teeHaku(skip, facetQuery);
		}
		
		var hakusanat = $('#pta-haku-input-container input').val();
		var sort = readSortFromUI();
		console.log('sort', sort);

		var queryArray = (hakusanat.match(/(?:[^\s"]+|"[^"]*")+/g) || []).map(str => str.replace(/"/g,'')).filter(function(v) { return v.length > 0; });
		
		var query = {
			query: queryArray,
			skip: skip,
			pageSize: pageSize,
			facets: facetQuery,
			sort: sort
		};

		var vinkit = $('#pta-tulokset #pta-tulokset-vinkit');
		vinkit.hide();

		var osumat = $('#pta-tulokset #pta-tulokset-osumat');
		osumat.hide();

		var fasetit = $('#pta-tulokset #pta-tulokset-fasetit');
		fasetit.hide();

		var virhe = $('#pta-tulokset #pta-tulokset-virhe');
		virhe.hide();
		
		
		$.ajax({
			url: 'v1/search',
			method: 'POST',
			data: JSON.stringify(query),
			contentType: 'application/json',
			dataType: 'json'
		}).done(function(result) {
		
			// Vinkit
			var vinkkiLista = $('#pta-tulokset-vinkit-lista', vinkit);
			vinkkiLista.empty();
			result.hints.forEach(function(vinkki) {
				var tmp = $('<div></div>');
				tmp.addClass('pta-tulokset-vinkit-vinkki');
				tmp.text(vinkki);
				tmp.click(function() {
				    var text = $('#pta-haku-input-container input').val();
				    text += ' ';
				    text += vinkki;
				    $('#pta-haku-input-container input').val(text);
				    teeHaku(0, facetQuery);
				    
				});
				vinkkiLista.append(tmp);
			});
			vinkit.show();

			// Tulokset
			var osumaLista = $('#pta-tulokset-osumat-lista', osumat);
			osumaLista.empty();
			result.hits.forEach(function(osuma) {
				var tmp = $('<div></div>');
				tmp.addClass('pta-tulokset-osumat-osuma');
				
				var title = $('<p></p>');
				var text = osuma.text.find( d => d.lang === lang && d.title) || osuma.text[0] || {};
				
				title.text(text.title + ' ('+Math.round(osuma.score*100)/100+')');
				title.addClass('pta-tulokset-osumat-osuma-title');
				tmp.append(title);
				
				var desc = $('<div></div>');
				desc.addClass('pta-tulokset-osumat-osuma-desc');
				desc.text(text.abstractText);
				desc.hide();
				title.click(function() { desc.toggle(); });
				desc.click(function() { desc.toggle(); });
				
				tmp.append(desc);
				
				var link = $('<a></a>');
				link.text('Avaa katalogissa');
				if (osuma.catalog.type === "CSW") {
					link.attr('href', osuma.catalog.url + '/geonetwork/srv/eng/catalog.search#/metadata/' + osuma.id);
				} else if (osuma.catalog.type === "CKAN") {
					if (osuma.types.includes("isService")) {
						link.attr('href', osuma.catalog.url + '/api/3/action/package_show?id=' + osuma.id);
					} else {
						link.attr('href', osuma.catalog.url + '/api/3/action/resource_show?id=' + osuma.id);
					}
				} else {
					link.attr('href', 'unknown_type')
				}

				link.attr('target', '_blank');
				
				tmp.append(link);
				
				//tmp.text('foo: '+JSON.stringify(osuma));
				osumaLista.append(tmp);
			});
			
			if (skip > 0) {
				osumaLista.append($('<span class="pta-tulokset-osumat-previous">Edelliset</span>').click(function() {
					teeHaku(skip-pageSize, facetQuery);
				}));
			}

			if ((skip + result.hits.length) < result.totalHits) {
				osumaLista.append($('<span class="pta-tulokset-osumat-next">Seuraavat</span>').click(function() {
					teeHaku(skip+pageSize, facetQuery);
				}));
			}
			
			osumat.show();


			// Fasetit
			var fasettiLista = $('#pta-tulokset-fasetit-lista', fasetit);
			fasettiLista.empty();

			for (var fasetti in result.facets) {
				var div = $('<div></div>');
				div.append($('<h4></h4>').text(fasetti));
				var table = $('<table></table>');
				
				result.facets[fasetti].forEach(function(d) {
					var f = fasetti;
					var row = $('<tr><td>'+d.id+'</td><td>'+d.count+'</td></tr>');
					row.on('click', function() {
						facetQuery[f] = facetQuery[f] || [];
						facetQuery[f].push(d.id);
						teeHaku(skip, facetQuery);
					});
				    table.append(row);
				});
				div.append(table);
				
				fasettiLista.append(div);	
			}
						
			
			fasetit.show();

		}).fail(function(err) {
			virhe.empty();
			console.error(err);
			var tmp = $('<div></div>');
			tmp.addClass('pta-virhe');
			tmp.text('Tapahtui virhe: '+err.statusText);

			var pre = $('<pre></pre>');
			pre.text(err.responseText);
			tmp.append(pre);

			virhe.append(tmp);
			virhe.show();
		});
	}


	//teeHaku();

});
