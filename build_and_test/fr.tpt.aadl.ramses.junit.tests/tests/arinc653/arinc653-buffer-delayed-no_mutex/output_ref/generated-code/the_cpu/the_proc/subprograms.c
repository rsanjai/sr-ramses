#include "subprograms.h"
test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_BA_State_t the_proc_the_sender_entrypoint_impl_current_state = test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_BA_entrypoint_init_state;
void test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl(test_buffer_delayed_refined_model__bufferArrayImpl_the_receiver_p_in_impl *  cnx_p_out)
{
  RETURN_CODE_TYPE test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_the_proc_the_sender_runtime_call_ret;
  common_pkg__Integer test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_p_out_localVariable;
  Base_Types__Integer_16 test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_the_proc_the_sender_IterationCounter = 0;
  Base_Types__Integer_16 test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_the_sender_p_out_CDWSize_eventdata_receiver_p_in = 3;
  test_buffer_delayed_refined_model__IndexArrayType_the_sender_p_out test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_the_sender_p_out_CDWTable_eventdata_receiver_p_in = {0,1,2};
  test_buffer_delayed_refined_model__the_proc_the_sender_behaviorIdentifier_enum whichPortActivated = test_buffer_delayed_refined_model__the_proc_the_sender_behaviorIdentifier_enum_default_behavior;
while(1)
{
  switch(the_proc_the_sender_entrypoint_impl_current_state)
  {
    case test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_BA_entrypoint_init_state:
    // Transition id: which_behavior_default_mode
    if(1) // no execution condition
    {
      the_proc_the_sender_entrypoint_impl_current_state = test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_BA_entrypoint_exec_state;
      whichPortActivated = test_buffer_delayed_refined_model__the_proc_the_sender_behaviorIdentifier_enum_default_behavior;
      break;
    }
    case test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_BA_entrypoint_wait_dispatch_state:
    // Transition id: dispatch_transition
    if(1) // no execution condition
    {
      the_proc_the_sender_entrypoint_impl_current_state = test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_BA_entrypoint_exec_state;
      PERIODIC_WAIT (&test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_the_proc_the_sender_runtime_call_ret);
      break;
    }
    case test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_BA_entrypoint_exec_state:
    // Transition id: call -- Priority 0
    if(whichPortActivated == test_buffer_delayed_refined_model__the_proc_the_sender_behaviorIdentifier_enum_default_behavior)
    {
      the_proc_the_sender_entrypoint_impl_current_state = test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_BA_entrypoint_wait_dispatch_state;
      send (&test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_p_out_localVariable);
      test_buffer_delayed_refined_model__Put_Value_the_proc_the_sender_p_out (&test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_the_proc_the_sender_IterationCounter, test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_the_sender_p_out_CDWTable_eventdata_receiver_p_in, test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_the_sender_p_out_CDWSize_eventdata_receiver_p_in, test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_p_out_localVariable, cnx_p_out);
      PeriodicDelayed_runtime__Send_Output (&test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_the_proc_the_sender_IterationCounter, test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl_the_sender_p_out_CDWSize_eventdata_receiver_p_in);
      break;
    }
  }
}
}
test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_BA_State_t the_proc_the_receiver_entrypoint_impl_current_state = test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_BA_entrypoint_init_state;
void test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl(test_buffer_delayed_refined_model__bufferArrayImpl_the_receiver_p_in_impl *  p_in)
{
  RETURN_CODE_TYPE test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_the_proc_the_receiver_runtime_call_ret;
  RETURN_CODE_TYPE test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_p_in_ErrorCode = 0;
  common_pkg__Integer test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_p_in_localVariable;
  Base_Types__Integer_16 test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_the_proc_the_receiver_IterationCounter = 0;
  Base_Types__Integer_16 test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_p_in_CPRSize = 6;
  Base_Types__Integer_16 test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_p_in_lastIndex = -1;
  Base_Types__Integer_16 test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_p_in_readIndex = -1;
  Base_Types__Integer_16 test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_the_receiver_p_in_BufferSize = 3;
  test_buffer_delayed_refined_model__IndexArrayType_the_receiver_p_in test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_p_in_CPRTable = {1,1,2,2,0,0};
  test_buffer_delayed_refined_model__the_proc_the_receiver_behaviorIdentifier_enum whichPortActivated = test_buffer_delayed_refined_model__the_proc_the_receiver_behaviorIdentifier_enum_default_behavior;
while(1)
{
  switch(the_proc_the_receiver_entrypoint_impl_current_state)
  {
    case test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_BA_entrypoint_init_state:
    // Transition id: which_behavior_default_mode
    if(1) // no execution condition
    {
      the_proc_the_receiver_entrypoint_impl_current_state = test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_BA_entrypoint_exec_state;
      whichPortActivated = test_buffer_delayed_refined_model__the_proc_the_receiver_behaviorIdentifier_enum_default_behavior;
      break;
    }
    case test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_BA_entrypoint_wait_dispatch_state:
    // Transition id: dispatch_transition
    if(1) // no execution condition
    {
      the_proc_the_receiver_entrypoint_impl_current_state = test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_BA_entrypoint_exec_state;
      PERIODIC_WAIT (&test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_the_proc_the_receiver_runtime_call_ret);
      break;
    }
    case test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_BA_entrypoint_exec_state:
    // Transition id: call -- Priority 0
    if(whichPortActivated == test_buffer_delayed_refined_model__the_proc_the_receiver_behaviorIdentifier_enum_default_behavior)
    {
      the_proc_the_receiver_entrypoint_impl_current_state = test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_BA_entrypoint_wait_dispatch_state;
      test_buffer_delayed_refined_model__Receive_Input_lowet_the_proc_the_receiver_p_in (&test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_the_proc_the_receiver_IterationCounter, test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_p_in_CPRTable, test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_p_in_CPRSize, &test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_p_in_readIndex, &test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_p_in_lastIndex, test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_the_receiver_p_in_BufferSize, &test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_p_in_ErrorCode);
      test_buffer_delayed_refined_model__Next_Value_the_proc_the_receiver_p_in (&test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_p_in_readIndex, test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_p_in_lastIndex, (*p_in), &test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_p_in_localVariable, test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_the_receiver_p_in_BufferSize, &test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_p_in_ErrorCode);
      if (test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_p_in_ErrorCode == 0) {
      receive (test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl_p_in_localVariable);
      }
      break;
    }
  }
}
}
test_buffer_delayed_refined_model__Put_Value_the_proc_the_sender_p_out_BA_State_t Put_Value_the_proc_the_sender_p_out_current_state = test_buffer_delayed_refined_model__Put_Value_the_proc_the_sender_p_out_s;
void test_buffer_delayed_refined_model__Put_Value_the_proc_the_sender_p_out(Base_Types__Integer_16 *  iteration_counter, PeriodicDelayed_runtime__ArrayDataType CDW, Base_Types__Integer_16 CDWSize, common_pkg__Integer pvalue, test_buffer_delayed_refined_model__bufferArrayImpl_the_receiver_p_in_impl *  buffer)
{
  while(1)
{
  switch(Put_Value_the_proc_the_sender_p_out_current_state)
  {
    case test_buffer_delayed_refined_model__Put_Value_the_proc_the_sender_p_out_s:
    // Transition id: t
    if(1) // no execution condition
    {
      Put_Value_the_proc_the_sender_p_out_current_state = test_buffer_delayed_refined_model__Put_Value_the_proc_the_sender_p_out_s;
      (*buffer)[CDW[(*iteration_counter)]] = pvalue;
      return;
    }
  }
}
}
PeriodicDelayed_runtime__Send_Output_BA_State_t Send_Output_current_state = PeriodicDelayed_runtime__Send_Output_s;
void PeriodicDelayed_runtime__Send_Output(Base_Types__Integer_16 *  iteration_counter, Base_Types__Integer_16 CDWSize)
{
  while(1)
{
  switch(Send_Output_current_state)
  {
    case PeriodicDelayed_runtime__Send_Output_s:
    // Transition id: t
    if(1) // no execution condition
    {
      Send_Output_current_state = PeriodicDelayed_runtime__Send_Output_s;
      (*iteration_counter) = ((*iteration_counter) + 1) % CDWSize;
      return;
    }
  }
}
}
test_buffer_delayed_refined_model__Receive_Input_lowet_the_proc_the_receiver_p_in_BA_State_t Receive_Input_lowet_the_proc_the_receiver_p_in_current_state = test_buffer_delayed_refined_model__Receive_Input_lowet_the_proc_the_receiver_p_in_s;
void test_buffer_delayed_refined_model__Receive_Input_lowet_the_proc_the_receiver_p_in(Base_Types__Integer_16 *  iteration_counter, PeriodicDelayed_runtime__ArrayDataType CPR, Base_Types__Integer_16 CPRSize, Base_Types__Integer_16 *  first, Base_Types__Integer_16 *  last, Base_Types__Integer_16 bufferSize, PeriodicDelayed_runtime__ReturnCodeType *  ReturnCode)
{
  Base_Types__Integer_16 next;
while(1)
{
  switch(Receive_Input_lowet_the_proc_the_receiver_p_in_current_state)
  {
    case test_buffer_delayed_refined_model__Receive_Input_lowet_the_proc_the_receiver_p_in_s:
    // Transition id: t2
    if(1) // no execution condition
    {
      Receive_Input_lowet_the_proc_the_receiver_p_in_current_state = test_buffer_delayed_refined_model__Receive_Input_lowet_the_proc_the_receiver_p_in_s;
      next = ((*iteration_counter) + 1) % CPRSize;
      if (CPR[next] != CPR[(*iteration_counter)]) {
      (*first) = CPR[(*iteration_counter)];
      (*last) = CPR[next];
      }
      else {
      (*first) = -1;
      (*last) = -1;
      }
      (*iteration_counter) = ((*iteration_counter) + 1) % CPRSize;
      return;
    }
  }
}
}
test_buffer_delayed_refined_model__Next_Value_the_proc_the_receiver_p_in_BA_State_t Next_Value_the_proc_the_receiver_p_in_current_state = test_buffer_delayed_refined_model__Next_Value_the_proc_the_receiver_p_in_s;
void test_buffer_delayed_refined_model__Next_Value_the_proc_the_receiver_p_in(Base_Types__Integer_16 *  first, Base_Types__Integer_16 last, test_buffer_delayed_refined_model__bufferArrayImpl_the_receiver_p_in_impl buffer, common_pkg__Integer *  output, Base_Types__Integer_16 bufferSize, PeriodicDelayed_runtime__ReturnCodeType *  ReturnCode)
{
  while(1)
{
  switch(Next_Value_the_proc_the_receiver_p_in_current_state)
  {
    case test_buffer_delayed_refined_model__Next_Value_the_proc_the_receiver_p_in_s:
    // Transition id: t
    if(1) // no execution condition
    {
      Next_Value_the_proc_the_receiver_p_in_current_state = test_buffer_delayed_refined_model__Next_Value_the_proc_the_receiver_p_in_s;
      if ((*first) >= 0) {
      (*output) = buffer[(*first)];
      if ((*first) != last) {
      (*first) = ((*first) + 1) % bufferSize;
      (*ReturnCode) = PeriodicDelayed_runtime__ReturnCodeType_Ok;
      }
      else {
      (*ReturnCode) = PeriodicDelayed_runtime__ReturnCodeType_EmptyQueue;
      }
      }
      else {
      (*ReturnCode) = PeriodicDelayed_runtime__ReturnCodeType_EmptyQueue;
      }
      return;
    }
  }
}
}
