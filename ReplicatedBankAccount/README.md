Distributed systems assignment 2:

   Client commands:
   
   - [x] getQuickBalance
   - [ ] getSyncedBalance
   - [x] deposit <amount> (using the spread daemon)
   - [x] addInterest <percent> (using the spread daemon)
   - [ ] getHistory 
   - [ ] checkTxStatus <Transaction.unique_id>
   - [ ] cleanHistory
   - [x] memberInfo
   - [x] sleep <duration>
   - [x] exit 

Other things that need doing:


- [ ] synchronize the state for a newly joined replica
- [ ] make it possible to read commands from file
     
Explanation of why synchronizing a newly joined replica is quite complicated:

If we send a state update right away we might go out of sync
Example scenario: We have 1 replica (R1) with 100$ in the account the timeline is as follows:
1. R1 sends "addInterest 10" to spread
2. R2 joins the group
3. R1 gets the membership message, sends state update to R2
4. R1 gets "addInterest 10" back from spread, to applies it
5. R2 gets the "addInterest 10" but right now the balance is 0
so it will still be 0 
6. R2 gets the state update from R1, which was generated before 
the interest was applied, meaning the new balance will be 100.

In this moment, R1 thinks the account has 110 and R2 thinks the account
has 100.

Solution:
Each replica can only send the state update when there are no
transactions that have been sent to Spread and we did not receive
the message for them. In the example above, at step 5, R2 will 
simply ignore the message because the Snapshot will see
that the message from R1 has an outstandingCounter which we did not expect
but that is ok, we know that R1 is going to send a state update 
after applying the addInterest 10. The state update class contains:
balance, orderCount (both "global") and also the outstandingCounter
which is specific to the sender (R1 in our case).

