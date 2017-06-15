
# Aktualizace objektu  #

Updates Echo neboli informace o aktualizaci objektu je JSON zpráva. Update je vždy zaslán pokud je na 
Model objektu změněn jeho parametr a volána funkce @Override public void update() {..}


     { 
            "messageType":  "object_update",
            "id": "1",                      
            "messageChannel": "becki",
         
            "model": "CProgram",
            "model_id": "1231312341234124234231e",
            
     }

####  Seznam objektů, které mohou přijít skrz Updates Echo (websocket) #### 
  
   * Project
   * Person
   * Version_Object (B_Program_Version, C_Program_Version)
   * BProgram
   * Board
   * CProgram
   * Product
   * HomerInstance
   * ActualizationProcedure
   * Invoice
  
   
v Becki se plánuje stromová struktura objektu - takže stačí najít objekt se stejným ID a ten refreshovat.
 Není tak  nutné rozumnět každému modelu (typu objektu) ale jen refreshovat daný s ID (UUID)  

