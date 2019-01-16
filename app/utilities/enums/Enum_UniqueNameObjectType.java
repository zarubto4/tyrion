package utilities.enums;

public enum Enum_UniqueNameObjectType {

    Project,

    HomerServer,
    CodeServer,

    BProgram,
    BProgramVersion,

    CProgram,
    CProgramVersion,

    CLibrary,       // TODO - až budeme integrovat knihovny
    CLibraryVersion,// TODO - až budeme integrovat knihovny

    GridProgram,        // Vyžaduje objectID
    GridProgramVersion, // Vyžaduje objectID
    GridProject,

    Hardware,
    HardwareGroup,

    GSM,
    Role,


    Widget,
    WidgetVersion,
    Block,
    BlockVersion,
    Instance,

    Snapshot,   // Vyžaduje objectID

    Database,
    DatabaseCollection

}
