package fi.maanmittauslaitos.pta.search.api;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HakuController {
	
	@Autowired
	private HakuKone hakukone;

	@RequestMapping(value = "/v1/search", method = RequestMethod.POST)
	public HakuTulos hae(@RequestBody HakuPyynto pyynto) throws IOException
	{
		HakuTulos tulos = hakukone.haku(pyynto);
		
		return tulos;
	}
}
