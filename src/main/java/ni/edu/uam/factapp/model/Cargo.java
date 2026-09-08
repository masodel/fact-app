package ni.edu.uam.factapp.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cargo {
    private Integer id;
    private String nombre;
    private String descripcion;
}
