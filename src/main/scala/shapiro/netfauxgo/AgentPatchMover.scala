package shapiro.netfauxgo

import akka.transactor._

class AgentPatchMover extends Transactor {
  // transact movement between patches
  override def coordinate = {
    case MovePatches(agent, from, to) =>
      sendTo(from -> AgentLeft(agent), to -> AgentEntered(agent))
  }

  def atomically = implicit txn => {
    case message =>
  }

}