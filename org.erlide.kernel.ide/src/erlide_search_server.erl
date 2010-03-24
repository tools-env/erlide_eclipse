%% Description: Search server
%% Author: jakob (jakobce at g mail dot com)
%% Created: 10 mar 2010

-module(erlide_search_server).

%%
%% Include files
%%

%% -define(DEBUG, 1).

-include("erlide.hrl").
-include("erlide_scanner.hrl").

-include_lib("kernel/include/file.hrl").

-include("erlide_search_server.hrl").

%%
%% Exported Functions
%%


%% called from Java
-export([start/0, 
         stop/0,
         %% add_modules/1,
         find_refs/3,
         find_refs/4,
         find_refs/5,
         state/0]).

%% called from Erlang
-export([remove_module/1,
         add_module_refs/2]).

%%
%% Internal Exports
%%

-export([loop/1]).

%%
%% Macros and Records
%%

-define(SERVER, erlide_search_server).

-record(state, {modules=[], dummy}). %% FIXME still too simple data model
-record(module, {scanner_name, refs}).

%%
%% API Functions
%%

start() ->
    start(whereis(?SERVER)).

stop() ->
    server_cmd(stop).

%% add_modules(Modules) ->
%%     R = server_cmd(add_modules, Modules),
%%     ?D(state()),
%%     R.

state() ->
    server_cmd(state).

%% modules is {ScannerName, ModulePath}
find_refs(Ref, Modules, StateDir) ->
    R = server_cmd(find_refs, {Ref, Modules, StateDir}),
    ?D(R),
    R.

find_refs(macro, M, Modules, StateDir) ->
    find_refs({macro_ref, M}, Modules, StateDir);
find_refs(record, R, Modules, StateDir) ->
    find_refs({record_ref, R}, Modules, StateDir);
find_refs(include, F, Modules, StateDir) ->
    find_refs({include, F}, Modules, StateDir).

find_refs(M, F, A, Modules, StateDir) 
  when is_atom(M), is_atom(F), is_integer(A), is_list(Modules), 
       is_list(StateDir) ->
    find_refs({external_call, M, F, A}, Modules, StateDir).

remove_module(ScannerName) ->
    server_cmd(remove_module, ScannerName).

add_module_refs(ScannerName, Refs) ->
    server_cmd(add_module_refs, {ScannerName, Refs}).

%%
%% Local Functions
%%

start(undefined) ->
    Self = self(),
    spawn(fun() ->
                  ?SAVE_CALLS,
                  erlang:yield(),
                  erlang:register(?SERVER, self()),
                  Self ! started,
                  loop(#state{})
          end),
    receive
        started ->
            ok
        after 10000 ->
            {error, timeout_waiting_for_search_server}
    end;
start(_) ->
    ok.

server_cmd(Command) ->
    server_cmd(Command, []).

server_cmd(Command, Args) ->
    start(),
    try
        ?SERVER ! {Command, self(), Args},
        receive
            {Command, _Pid, Result} ->
                Result
        end
    catch _:Exception ->
              {error, Exception}
    end.


loop(State) ->
    ?D(State),
    receive
        {stop, From, []} ->
            reply(stop, From, stopped);
        {Cmd, From, Args} ->
            ?D(Cmd),
            NewState = cmd(Cmd, From, Args, State),
            ?D(NewState),
            ?MODULE:loop(NewState)
    end.

cmd(Cmd, From, Args, State) ->
    try
        case get(logging) of
            on ->
                put(log, get(log)++[{Cmd, Args}]);
            _ ->
                ok
        end,
        case do_cmd(Cmd, Args, State) of
            {R, NewState} ->
                reply(Cmd, From, R),
                NewState;
            ok ->
                reply(Cmd, From, ok),
                State;
            NewState ->
                reply(Cmd, From, ok),
                NewState
        end
    catch
        exit:Error ->
            reply(Cmd, From, {exit, Error}),
            State;
        error:Error ->
            reply(Cmd, From, {error, Error}),
            State
    end.

reply(Cmd, From, R) ->
    From ! {Cmd, self(), R}.

do_cmd(add_module_refs, {Module, Refs}, State) ->
    do_add_module_refs(Module, Refs, State);
do_cmd(find_refs, {Ref, Modules, StateDir}, State) ->
    ?D(Ref),
    ?D(Modules),
    ?D(State),
    R = do_find_refs(Modules, Ref, StateDir, State, []),
    ?D(R),
    R;
do_cmd(state, _, State) ->
    {State, State}.

do_find_refs([], _, _, State, Acc) ->
    {{ok, Acc}, State};
do_find_refs([{ScannerName, ModulePath} | Rest], Ref, StateDir, 
             #state{modules=Modules} = State, Acc0) ->
    ?D(ScannerName),
    Refs = get_module_refs(ScannerName, ModulePath, StateDir, Modules),
    ?D(Refs),
    Acc1 = find_data(Refs, Ref, ModulePath, Acc0),
    ?D(Acc1),
    do_find_refs(Rest, Ref, StateDir, State, Acc1);
do_find_refs(A, B, C, D, E) ->
    ?D({A, B, C, D, E}),
    erlang:exit(badarg).

find_data([], _, _, Acc) ->
    Acc;
find_data([#ref{function=F, arity=A, clause=C, data=D, offset=O, length=L, sub_clause=S} | Rest],
          Data, M, Acc) ->
    case D of
        Data ->
            find_data(Rest, Data, M, [{M, F, A, C, S, O, L} | Acc]);
        _ ->
            find_data(Rest, Data, M, Acc)
    end.

get_module_refs(ScannerName, ModulePath, StateDir, Modules) ->
    ?D(Modules),
    case lists:keysearch(ScannerName, #module.scanner_name, Modules) of
        {value, {ScannerName, #module{refs=Refs}}} ->
            ?D(ye),
            Refs;
        false ->
            ?D(ok),
            read_module_refs(ScannerName, ModulePath, StateDir)
    end.

read_module_refs(ScannerName, ModulePath, StateDir) ->
    erlide_noparse:read_module_refs(ScannerName, ModulePath, StateDir).

do_add_module_refs(Module, Refs, #state{modules=Modules0} = State) ->
    Modules1 = lists:keydelete(Module, #module.scanner_name, Modules0),
    Modules2 = [[#module{scanner_name=Module, refs=Refs}] | Modules1],
    State#state{modules=Modules2}.
