#include "activity.h"
#include "main.h"

extern test_buffer_delayed_refined_model__SharedStructType_the_receiver_p_in_impl the_receiver_p_in_globalVariable;
extern SEMAPHORE_ID_TYPE the_proc_the_receiver_p_in_sem;
void test_buffer_delayed_refined_model__the_proc_the_sender_impl_Init()
{
}

void* test_buffer_delayed_refined_model__the_proc_the_sender_impl_Job()
{
  while (1) {
    test_buffer_delayed_refined_model__the_proc_the_sender_entrypoint_impl (&the_receiver_p_in_globalVariable, &the_proc_the_receiver_p_in_sem);
  }
  return 0;
}
extern test_buffer_delayed_refined_model__SharedStructType_the_receiver_p_in_impl the_receiver_p_in_globalVariable;
extern SEMAPHORE_ID_TYPE the_proc_the_receiver_p_in_sem;
void test_buffer_delayed_refined_model__the_proc_the_receiver_impl_Init()
{
  Base_Types__Integer_16 test_buffer_delayed_refined_model__the_proc_the_receiver_impl_the_proc_the_receiver_p_in_BufferSize = 10;
  test_buffer_delayed_refined_model__p_in_Init_Spg (&the_receiver_p_in_globalVariable, test_buffer_delayed_refined_model__the_proc_the_receiver_impl_the_proc_the_receiver_p_in_BufferSize);
}

void* test_buffer_delayed_refined_model__the_proc_the_receiver_impl_Job()
{
  while (1) {
    test_buffer_delayed_refined_model__the_proc_the_receiver_entrypoint_impl (&the_receiver_p_in_globalVariable, &the_proc_the_receiver_p_in_sem);
  }
  return 0;
}