package pl.clinic.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.clinic.project.entities.PatientEntity;
import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository <PatientEntity, Integer> {

    List<PatientEntity> findAllByPhoneNumber(String phone);

    List<PatientEntity> findAllByPeselNumber(String pesel);

}
