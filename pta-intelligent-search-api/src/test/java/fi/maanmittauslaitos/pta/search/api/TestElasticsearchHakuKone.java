package fi.maanmittauslaitos.pta.search.api;

public class TestElasticsearchHakuKone {
	public static void main(String[] args) throws Exception
	{
		/* Disabloitu kunnes tätä oikeasti tarvitaan
		SpringApplication app = new SpringApplication(TestElasticsearchHakuKone.class);
		
        app.setWebEnvironment(false); 
        ConfigurableApplicationContext ctx = app.run(args);
        
        HakuKone hakukone = ctx.getBean(HakuKone.class);
		
		HakuPyynto pyynto = new HakuPyynto();
		pyynto.getHakusanat().add("kissa");
		
		HakuTulos tulos = hakukone.haku(pyynto);
		
		System.out.println("Osumat ("+tulos.getOsumat().size()+" kpl)");
		for (Osuma osuma : tulos.getOsumat()) {
			System.out.println("\t"+osuma.getUrl()+" ("+osuma.getRelevanssi()+")");
		}
		
		System.out.println("Vinkit ("+tulos.getHakusanavinkit().size()+" kpl)");
		System.out.println("\t"+tulos.getHakusanavinkit());
		*/
	}
}
