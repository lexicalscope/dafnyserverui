package com.lexicalscope.dafny.dafnyservergui;

import com.lexicalscope.jewel.cli.Option;

public interface Arguments
{
  @Option(description="the location of the dafny server")
  String server();

  @Option(description="the file you wish to verify")
  String file();
}