description: Frequently asked questions FAQ and answers for troubleshooting Pantheon use
<!--- END of page meta data -->

# Troubleshooting

If Pantheon is not working as expected, here are some things to check or try. 

## Command Line Options Not Working as Expected

Characters such as smart quotes and long (em) hyphens won't work in Pantheon options. Ensure that quotes have
not been automatically converted to smart quotes or that double hyphens have not been combined into em hyphens.

## Missing Command Line Options 

Docker

## Genesis Milestone 

Not including a sufficient milestone in the genesis file can lead to unexpected and inconsistent behaviour without
specific errors. Ensure a sufficient milestone is included in the genesis file (for example, `constantinoplefixblock`). 

## Logging 

Restart Pantheon with the [`--logging=DEBUG`](../Reference/Pantheon-CLI-Syntax.md) and look at the log files. 

## Host Not Authorized 

Set [`--host-whitelist`]

## Ports

opening ports 

does the bootnode have port 30303 publicly open? I.e. have you got ports forwarded etc. through your network?
from the remote node, can you telnet to port 30303 on your bootnode? i.e. telnet 14.142.150.34 30303?
Are your nodes running in AWS? If so, do you have appropriate SecurityGroups which allow access to port 30303?

 if you set p2p-host to 127.0.0.1 I believe you will only receive connections from the local machine - if you want to accept connections from remote peers you will need to set it to 0.0.0.0 