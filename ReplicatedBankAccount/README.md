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


- [x] synchronize the state for a newly joined replica
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

Candidate Solution:
The existing replicas send a full snapshot of what they know and the newly
joined replicas wait to receive a snapshot before applying any transactions.
If transactions happen to come before they receive snapshots, 
the new replica will just store the transaction info for later, but it will
also check them against the snapshot it will eventually receive.

