package edu.iis.mto.testreactor.doser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import edu.iis.mto.testreactor.doser.infuser.Infuser;
import edu.iis.mto.testreactor.doser.infuser.InfuserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class MedicineDoserTest {

    @Mock
    Infuser infuser;

    @Mock
    Clock clock;

    @Mock
    DosageLog dosageLog;

    private MedicineDoser medicineDoser;
    private Medicine medicine;
    private Capacity capacity;
    MedicinePackage medicinePackage;
    private Dose basicDose;
    private Receipe basicRecipe;

    @BeforeEach
    void start(){
        medicineDoser =  new MedicineDoser(infuser,dosageLog,clock);
        medicine = medicine.of("LEK");
        capacity = capacity.of(10,CapacityUnit.MILILITER);
        medicinePackage = medicinePackage.of(medicine,capacity);
        medicineDoser.add(medicinePackage);

        basicDose = basicDose.of(capacity, Period.of(10, TimeUnit.MILLISECONDS));
        basicRecipe = basicRecipe.of(medicine,basicDose,1);
    }

    @Test
    void methodShouldReturnSuccessWhenAllRequirementsMeet() {


        assertEquals(DosingResult.SUCCESS, medicineDoser.dose(basicRecipe));
    }

    @Test
    void methodShouldReturnUnavailableMedicineExceptionWhenRequestedMedicineDoesNotExist(){
        Receipe receipe = null;
        Dose dose = null;
        Medicine medicine = null;
        medicine = medicine.of("NOTEXISTINGMEDICINE");


        dose = dose.of(capacity, Period.of(10, TimeUnit.MILLISECONDS));
        receipe = receipe.of(medicine,dose,1);

        Receipe finalReceipe = receipe;


        assertThrows(MedicineException.class, () -> medicineDoser.dose(finalReceipe));
    }

    @Test
    void methodShouldThrowInsufficientMedicineExceptionWhenRecipeDoseIsGreaterThanMedicineCappacity(){
        Receipe receipe = Receipe.of(medicine,basicDose,2);

        assertThrows(InsufficientMedicineException.class, () -> medicineDoser.dose(receipe));

    }

    @Test
    void methodCallsLogDifuserErrorRecipeNumberTimesWhenItReturnsException() throws InfuserException {

        doThrow(InfuserException.class).when(infuser).dispense(any(MedicinePackage.class),any(Capacity.class));

        medicineDoser.dose(basicRecipe);

        verify(dosageLog, times(basicRecipe.getNumber())).logDifuserError(any(),any());
    }

    @Test
    void methodShouldCallLogStartDoseAndLogEndDoseExactAmmountOfTimes(){

        medicineDoser.dose(basicRecipe);

        verify(dosageLog,times(basicRecipe.getNumber())).logStartDose(any(),any());
        verify(dosageLog,times(basicRecipe.getNumber())).logEndDose(any(),any());

    }

    @Test
    void methodShouldCallLogStartOnce(){
        medicineDoser.dose(basicRecipe);

        verify(dosageLog,times(1)).logStart();

    }

    @Test
    void methodSHouldCallLogEndOnce(){
        medicineDoser.dose(basicRecipe);

        verify(dosageLog,times(1)).logEnd();
    }
}
