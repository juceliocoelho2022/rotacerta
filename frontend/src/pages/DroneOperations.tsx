import { DroneFlightControlCenter } from '../components/DroneFlightControlCenter'
import { Drones } from './Drones'

export function DroneOperations() {
  return (
    <div className="droneOperationsStack">
      <DroneFlightControlCenter />
      <Drones />
    </div>
  )
}
