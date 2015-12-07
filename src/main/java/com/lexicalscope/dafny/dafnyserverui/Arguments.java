package com.lexicalscope.dafny.dafnyserverui;

import com.lexicalscope.jewel.cli.Option;

public interface Arguments
{
  @Option(description="the location of the dafny server")
  String server();

  @Option(description="the file you wish to verify")
  String file();

  @Option(description="time limit for verification of a procedure in seconds", defaultValue="30")
  int timeLimit();
}