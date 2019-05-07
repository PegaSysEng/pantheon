description: Frequently asked questions FAQ and answers for troubleshooting Pantheon
<!--- END of page meta data -->

# Troubleshooting

If Pantheon is not working as expected, here are some things to check or try. 

## Command Line Options

Characters such as smart quotes and long (em) hyphens won't work in Pantheon command line options. Ensure quotes have
not been automatically converted to smart quotes or double hyphens combined into em hyphens.

## Missing Command Line Options 

When running Pantheon from the Docker image a number of [command line options are not used](../Getting-Started/Run-Docker-Image.md#command-line-options). 

## Genesis Milestone 

Not including a sufficient milestone in the genesis file can lead to unexpected and inconsistent behaviour without
specific errors. Ensure a sufficient milestone is included in the genesis file (for example, `constantinoplefixblock`). 

## Logging 

Restart Pantheon with the command line option [`--logging=DEBUG`](../Reference/Pantheon-CLI-Syntax.md#logging) and look at the log files. 

## Host Not Authorized 

If you are receiving a `Host not authorized` error when attempting to access the JSON-RPC API, ensure [`--host-whitelist`](../Reference/Pantheon-CLI-Syntax.md#host-whitelist)
is set appropriately. 

## Ports

If nodes are not communicating, ensure the [required ports are open](https://docs.pantheon.pegasys.tech/en/stable/Configuring-Pantheon/Networking/Configuring-Ports/). 

If your nodes are running in AWS, check you have appropriate `SecurityGroups` to allow access to the required ports.
 
## New Line Characters at the End of Files

Ensure there is no new line character (`\n`) at the end of password files. New line characters may not 
be displayed in all editors. 