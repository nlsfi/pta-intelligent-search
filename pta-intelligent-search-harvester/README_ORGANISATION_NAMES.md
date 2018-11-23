# Organisation names

This harvester canonicalizes organisation names based on a ODS sheet. The goal of this canonicalization is to harmonize multiple different variants of a name to a single name to help users and the facet search.

The file is loaded from
1. The file path pointed to by the system environment property CANONICAL_ORGANISATIONS_FILE
2. The file "canonical_organisations.ods" in the current directory of the JVM process
3. The resource "/canonical_organisations.ods" from classpath (included in the repository)

The file is organized into three sheets, one for each language ("fi", "sv", "en"). The name of the tab is used as the language code. 

First row of each sheet is ignored, it can be used for headers, notes, etc. As to the columns, only the first two are used.

Column A contains canonical names for organisations and column B contains alternative names for that organisation. If column A is empty on the next row, then column B should contain an alternative name for the same organisation as for the previous row. If column A is set but B is empty, then no alternative names are available for that organisation.

| Header, ignore  | Header, ignore         |
|-----------------|------------------------|
| Org1            | Alt name A for Org1    |
|                 | Alt name B for Org1    |
| Org2            | Only alt name for Org2 |
| Org3            | Alt name A for Org3    |
|                 | Alt name B for Org3    |
