import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Characteristics(
    @SerialName("pr") var pr: Int,
    @SerialName("qrs") var qrs: Int,
    @SerialName("qt") var qt: Int,
    @SerialName("qtc") var qtc: Double,
    @SerialName("rr") var rr: Int,
    @SerialName("bpm") var heartRate: Int,
    @SerialName("st_elevation") var stElevation: Double,
    @SerialName("qrs_intervals") var qrsIntervals: List<Double>,
    @SerialName("rr_intervals") var rrIntervals: List<Double>,
    @SerialName("pr_stop_indices") var prStopIndices: List<Double>,
    @SerialName("pr_start_indices") var prStartIndices: List<Double>,
    @SerialName("p_wave_points") var pWavePoints: List<Double>,
    @SerialName("q_wave_points") var qWavePoints: List<Double>,
    @SerialName("s_wave_points") var sWavePoints: List<Double>,
    @SerialName("t_wave_points") var tWavePoints: List<Double>,
    @SerialName("r_peak_points") var rPeakPoints: List<Double>,
    @SerialName("t_wave_end_points") var tWaveEndPoints: List<Double>,

    // Added for new algorithm
    @SerialName("averagePAmplitude") var averagePAmplitudeInLead: Double,
    @SerialName("averageQAmplitude") var averageQAmplitudeInLead: Double,
    @SerialName("averageSAmplitude") var averageSAmplitudeInLead: Double,
    @SerialName("averageTAmplitude") var averageTAmplitudeInLead: Double,
    @SerialName("averageRAmplitude") var averageRAmplitudeInLead: Double,
    @SerialName("pWidth") var pWidth: Double,
    @SerialName("tWidth") var tWidth: Double,
    @SerialName("qrsDirectionUpward") var qrsDirectionUpward: Boolean,
    @SerialName("ratioRS") var ratioRS: Double,
    @SerialName("ventricularActivationLOR") var ventricularActivationLOR: Double,
    @SerialName("ventricularActivationROR") var ventricularActivationROR: Double,
    @SerialName("indication") var concavity: Boolean,
    @SerialName("frequencyOfPatterninQrsArray") var frequencyOfPatternInQRS: Int,
    @SerialName("frequencyOfPatterninRRArray") var frequencyOfPatternInRR: Int,
    @SerialName("pAmplitudeArrayInMv") var pAmplitudeArrayInMv: List<Double>,
    @SerialName("TRRatioSatisfy") var TRRatioSatisfy: Boolean,
    @SerialName("TSRatioSatisfy") var TSRatioSatisfy: Boolean
)  {
    override fun toString(): String {
        return "EcgCharacteristics(pr=$pr, qrs=$qrs, qt=$qt, qtc=$qtc, rr=$rr, heartRate=$heartRate, stElevation=$stElevation, qrsIntervals=$qrsIntervals, rrIntervals=$rrIntervals, prStopIndices=$prStopIndices, pWavePoints=$pWavePoints, qWavePoints=$qWavePoints, sWavePoints=$sWavePoints, tWavePoints=$tWavePoints, rPeakPoints=$rPeakPoints, tWaveEndPoints=$tWaveEndPoints, averagePAmplitudeInLead=$averagePAmplitudeInLead, averageQAmplitudeInLead=$averageQAmplitudeInLead, averageSAmplitudeInLead=$averageSAmplitudeInLead, averageTAmplitudeInLead=$averageTAmplitudeInLead, averageRAmplitudeInLead=$averageRAmplitudeInLead,pWdth=$pWidth,tWidth=$tWidth,qrsDirectionUpward=$qrsDirectionUpward,ratioRS=$ratioRS,ventricularActivationLOR=$ventricularActivationLOR,ventricularActivationROR=$ventricularActivationROR,concavity=$concavity,frequencyOfPatternInQRS=$frequencyOfPatternInQRS,frequencyOfPatternInRR=$frequencyOfPatternInRR,pAmplitudeArrayInMv=$pAmplitudeArrayInMv,TRRatioSatisfy=$TRRatioSatisfy,TSRatioSatisfy=$TSRatioSatisfy)"
    }
}
