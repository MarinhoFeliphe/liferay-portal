create unique index IX_C8D3E29 on CPDefinitionDiagramEntry (CPDefinitionId, sequence[$COLUMN_LENGTH:75$]);

create index IX_CD3A657 on CPDefinitionDiagramPin (CPDefinitionId);

create unique index IX_13D84BD2 on CPDefinitionDiagramSetting (CPDefinitionId);
create index IX_78A0FB77 on CPDefinitionDiagramSetting (uuid_[$COLUMN_LENGTH:75$], companyId);