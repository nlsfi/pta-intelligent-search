package fi.maanmittauslaitos.pta.search.metadata;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.metadata.model.MetadataAssociatedResource;
import fi.maanmittauslaitos.pta.search.metadata.model.MetadataDownloadLink;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ISOMetadataExtractor_RelatedResourcesTest extends BaseMetadataExtractorTest {


    @Test
    public void testMaastotietokanta_DownloadLinks() throws Exception {
        Document document = createMaastotietokantaDocument();

        List<MetadataDownloadLink> links = document.getListValue(ResultMetadataFields.DOWNLOAD_LINKS, MetadataDownloadLink.class);

        assertEquals(1, links.size());
        MetadataDownloadLink link = links.get(0);

        assertEquals("https://tiedostopalvelu.maanmittauslaitos.fi/tp/kartta", link.getUrl());
        assertEquals("Avoimien aineistojen tiedostopalvelu", link.getTitle());
        assertEquals("", link.getProtocol());
        assertEquals("", link.getDesc());
    }

    @Test
    public void testStatFi_ServiceAssociatedResources() throws Exception {
        Document document = createStatFiWFS();

        List<MetadataAssociatedResource> resources = document.getListValue(ResultMetadataFields.SERVICE_ASSOCIATED_RESOURCES, MetadataAssociatedResource.class);

        assertEquals(6, resources.size());

        MetadataAssociatedResource res1 = resources.get(0);
        MetadataAssociatedResource res2 = resources.get(1);
        MetadataAssociatedResource res3 = resources.get(2);
        MetadataAssociatedResource res4 = resources.get(3);
        MetadataAssociatedResource res5 = resources.get(4);
        MetadataAssociatedResource res6 = resources.get(5);

        assertEquals("isDataset", res1.getType());
        assertEquals("f6f81412-297e-45f9-ace3-bf3b1bb52a9f", res1.getMetadataId());
        assertNull(res1.getTitle());
        assertEquals("http://www.paikkatietohakemisto.fi/geonetwork/srv/en/csw?service=CSW&request=GetRecordById&version=2.0.2&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full&id=f6f81412-297e-45f9-ace3-bf3b1bb52a9f", res1.getUrl());

        assertEquals("isDataset", res2.getType());
        assertEquals("6a8b4061-7a48-4667-bbdb-13952726c79f", res2.getMetadataId());
        assertNull("", res2.getTitle());
        assertEquals("http://www.paikkatietohakemisto.fi/geonetwork/srv/en/csw?service=CSW&request=GetRecordById&version=2.0.2&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full&id=6a8b4061-7a48-4667-bbdb-13952726c79f", res2.getUrl());

        assertEquals("isDataset", res3.getType());
        assertEquals("b4693808-0f3b-4d5e-b366-c410b680ac19", res3.getMetadataId());
        assertNull(res3.getTitle());
        assertEquals("http://www.paikkatietohakemisto.fi/geonetwork/srv/en/csw?service=CSW&request=GetRecordById&version=2.0.2&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full&id=b4693808-0f3b-4d5e-b366-c410b680ac19", res3.getUrl());

        assertEquals("isDataset", res4.getType());
        assertEquals("d136a588-7fc6-4865-8d7c-b50d1398e728", res4.getMetadataId());
        assertNull(res4.getTitle());
        assertEquals("http://www.paikkatietohakemisto.fi/geonetwork/srv/en/csw?service=CSW&request=GetRecordById&version=2.0.2&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full&id=d136a588-7fc6-4865-8d7c-b50d1398e728", res4.getUrl());

        assertEquals("isDataset", res5.getType());
        assertEquals("ade7a36e-3beb-4e3d-821e-0652037e80f9", res5.getMetadataId());
        assertNull(res5.getTitle());
        assertEquals("http://www.paikkatietohakemisto.fi/geonetwork/srv/en/csw?service=CSW&request=GetRecordById&version=2.0.2&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full&id=ade7a36e-3beb-4e3d-821e-0652037e80f9", res5.getUrl());

        assertEquals("isDataset", res6.getType());
        assertEquals("5c938855-d6ca-40ed-aa9e-cf104f9563fc", res6.getMetadataId());
        assertNull(res6.getTitle());
        assertEquals("http://www.paikkatietohakemisto.fi/geonetwork/srv/en/csw?service=CSW&request=GetRecordById&version=2.0.2&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full&id=5c938855-d6ca-40ed-aa9e-cf104f9563fc", res6.getUrl());
    }
}
